-- PostgreSQL Schema and Logic Converted from Oracle

-- Sequences
CREATE SEQUENCE public.orders_seq START WITH 1 INCREMENT BY 1 OWNED BY public.orders.order_id;
CREATE SEQUENCE public.order_items_seq START WITH 1 INCREMENT BY 1 OWNED BY public.order_items.order_item_id;
CREATE SEQUENCE public.audit_log_seq START WITH 1 INCREMENT BY 1 OWNED BY public.audit_log.log_id;

-- Tables
CREATE TABLE public.customers (
    customer_id bigint PRIMARY KEY,
    email varchar(255) UNIQUE
);

CREATE TABLE public.products (
    product_id bigint PRIMARY KEY,
    sku varchar(64) UNIQUE
);

CREATE TABLE public.orders (
    order_id bigint PRIMARY KEY,
    customer_id bigint NOT NULL REFERENCES public.customers(customer_id),
    order_date timestamp DEFAULT CURRENT_TIMESTAMP,
    status varchar(30),
    total_amount numeric(12,2)
);

CREATE TABLE public.order_items (
    order_item_id bigint PRIMARY KEY,
    order_id bigint NOT NULL REFERENCES public.orders(order_id),
    product_id bigint NOT NULL REFERENCES public.products(product_id),
    quantity numeric,
    unit_price numeric(12,2)
);

CREATE TABLE public.inventory (
    product_id bigint PRIMARY KEY REFERENCES public.products(product_id),
    on_hand numeric,
    reserved numeric
);

CREATE TABLE public.audit_log (
    log_id bigint PRIMARY KEY,
    event_ts timestamp DEFAULT CURRENT_TIMESTAMP,
    actor varchar(128),
    action varchar(64),
    details text
);

CREATE TABLE public.stage_orders (
    batch_id varchar(40),
    ext_order_id varchar(64),
    customer_email varchar(255),
    product_sku varchar(64),
    quantity numeric,
    unit_price numeric(12,2),
    order_date date,
    currency char(3),
    op varchar(10)
);

-- Functions
CREATE OR REPLACE FUNCTION public.log_event(
    p_actor varchar,
    p_action varchar,
    p_details text
) RETURNS void AS $$
BEGIN
    INSERT INTO public.audit_log (log_id, actor, action, details)
    VALUES (nextval('public.audit_log_seq'), p_actor, p_action, p_details);
    -- No autonomous transaction; audit log is part of main transaction.
EXCEPTION
    WHEN OTHERS THEN
        -- Do not raise from logger; just swallow error.
        RAISE NOTICE 'Audit log failed: %', SQLERRM;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_customer_id(p_email varchar) RETURNS bigint AS $$
DECLARE
    l_id bigint;
BEGIN
    SELECT customer_id INTO l_id FROM public.customers WHERE LOWER(email) = LOWER(p_email);
    RETURN l_id;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_product_id(p_sku varchar) RETURNS bigint AS $$
DECLARE
    l_id bigint;
BEGIN
    SELECT product_id INTO l_id FROM public.products WHERE sku = p_sku;
    RETURN l_id;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.process_orders(
    p_stage_table text,
    p_batch_id text DEFAULT NULL,
    p_commit_interval integer DEFAULT 500,
    p_dry_run boolean DEFAULT FALSE,
    p_actor varchar DEFAULT current_user
) RETURNS void AS $$
DECLARE
    l_sql text;
    l_rows RECORD;
    l_limit integer := 500;
    l_processed integer := 0;
    l_committed integer := 0;
    l_customer_id bigint;
    l_product_id bigint;
    l_order_id bigint;
    l_status varchar(30);
BEGIN
    PERFORM public.log_event(p_actor, 'PROCESS_ORDERS_START',
        'stage='||p_stage_table||', batch_id='||COALESCE(p_batch_id,'(all)')||
        ', commit_every='||p_commit_interval||', dry_run='||(CASE WHEN p_dry_run THEN 'Y' ELSE 'N' END));

    FOR l_rows IN EXECUTE
        'SELECT batch_id, ext_order_id, customer_email, product_sku, quantity, unit_price, order_date, currency, op FROM ' ||
        quote_ident(p_stage_table) ||
        CASE WHEN p_batch_id IS NOT NULL THEN ' WHERE batch_id = $1' ELSE '' END
    USING p_batch_id
    LOOP
        l_customer_id := public.get_customer_id(l_rows.customer_email);
        l_product_id := public.get_product_id(l_rows.product_sku);

        IF l_customer_id IS NULL OR l_product_id IS NULL THEN
            PERFORM public.log_event(p_actor, 'STAGE_ROW_SKIPPED',
                'Reason='||CASE WHEN l_customer_id IS NULL THEN 'UNKNOWN_CUSTOMER' ELSE 'UNKNOWN_PRODUCT' END||
                ', ext_order_id='||l_rows.ext_order_id||', email='||l_rows.customer_email||', sku='||l_rows.product_sku);
            CONTINUE;
        END IF;

        IF UPPER(l_rows.op) = 'CANCEL' THEN
            BEGIN
                SELECT order_id, status INTO l_order_id, l_status
                FROM public.orders
                WHERE customer_id = l_customer_id
                  AND order_date = COALESCE(l_rows.order_date, order_date)
                  AND status <> 'CANCELLED'
                LIMIT 1
                FOR UPDATE;

                UPDATE public.orders SET status = 'CANCELLED' WHERE order_id = l_order_id;

                FOR oi IN SELECT product_id, quantity FROM public.order_items WHERE order_id = l_order_id LOOP
                    UPDATE public.inventory
                    SET on_hand = on_hand + oi.quantity,
                        reserved = GREATEST(reserved - oi.quantity, 0)
                    WHERE product_id = oi.product_id;
                END LOOP;

                PERFORM public.log_event(p_actor, 'ORDER_CANCELLED', 'order_id='||l_order_id||', ext='||l_rows.ext_order_id);
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    PERFORM public.log_event(p_actor, 'CANCEL_SKIP_NO_ORDER', 'ext='||l_rows.ext_order_id||', email='||l_rows.customer_email);
            END;
            CONTINUE;
        END IF;

        INSERT INTO public.orders (order_id, customer_id, order_date, status, total_amount)
        VALUES (nextval('public.orders_seq'), l_customer_id, l_rows.order_date, 'OPEN', 0)
        ON CONFLICT (customer_id, order_date)
        DO UPDATE SET status = 'OPEN'
        RETURNING order_id INTO l_order_id;

        INSERT INTO public.order_items (order_item_id, order_id, product_id, quantity, unit_price)
        VALUES (nextval('public.order_items_seq'), l_order_id, l_product_id, COALESCE(l_rows.quantity, 0), COALESCE(l_rows.unit_price, 0));

        UPDATE public.orders
        SET total_amount = (
            SELECT COALESCE(SUM(quantity * unit_price), 0)
            FROM public.order_items
            WHERE order_id = l_order_id
        )
        WHERE order_id = l_order_id;

        UPDATE public.inventory
        SET on_hand = GREATEST(on_hand - COALESCE(l_rows.quantity, 0), 0),
            reserved = COALESCE(reserved, 0) + COALESCE(l_rows.quantity, 0)
        WHERE product_id = l_product_id;

        l_processed := l_processed + 1;

        IF l_processed % p_commit_interval = 0 THEN
            IF p_dry_run THEN
                ROLLBACK;
            ELSE
                COMMIT;
            END IF;
            l_committed := l_committed + 1;
        END IF;
    END LOOP;

    IF p_dry_run THEN
        ROLLBACK;
        PERFORM public.log_event(p_actor, 'PROCESS_ORDERS_END', 'dry_run=Y, processed='||l_processed||', commits='||l_committed);
    ELSE
        COMMIT;
        PERFORM public.log_event(p_actor, 'PROCESS_ORDERS_END', 'dry_run=N, processed='||l_processed||', commits='||l_committed+1);
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        PERFORM public.log_event(p_actor, 'PROCESS_ORDERS_FATAL', SQLERRM);
        RAISE;
END;
$$ LANGUAGE plpgsql;
