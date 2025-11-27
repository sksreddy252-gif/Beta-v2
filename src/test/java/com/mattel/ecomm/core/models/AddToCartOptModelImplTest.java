package com.mattel.ecomm.core.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mattel.ecomm.core.models.AddToCartOptModelImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.aem.MockAemContext;
import io.wcm.testing.mock.aem.junit5.ResourceResolverType;

@ExtendWith(AemContextExtension.class)
class AddToCartOptModelImplTest {

    private static final String TEST_RESOURCE_PATH = "/content/test/addtocartopt";
    private static final String RESOURCE_TYPE = "mattel/ecomm/components/addtocartopt";

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
    private AddToCartOptModelImpl model;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(AddToCartOptModelImpl.class);
    }

    @Test
    void testValueMapValueGetters() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE,
                "ctaText", "Add to Cart",
                "ctaLink", "/cart",
                "ctaStyle", "primary");

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(AddToCartOptModelImpl.class);

        assertNotNull(model);
        assertEquals("Add to Cart", model.getCtaText());
        assertEquals("/cart", model.getCtaLink());
        assertEquals("primary", model.getCtaStyle());
    }

    @Test
    void testEmptyChildList() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(AddToCartOptModelImpl.class);

        List<?> options = model.getOptions();
        assertNotNull(options);
        assertTrue(options.isEmpty());
    }

    @Test
    void testChildListWithItems() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/options/option1",
                "label", "Option 1",
                "value", "opt1");
        context.create().resource(TEST_RESOURCE_PATH + "/options/option2",
                "label", "Option 2",
                "value", "opt2");

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(AddToCartOptModelImpl.class);

        List<AddToCartOptModelImpl.Option> options = model.getOptions();
        assertNotNull(options);
        assertEquals(2, options.size());
        assertEquals("Option 1", options.get(0).getLabel());
        assertEquals("opt1", options.get(0).getValue());
        assertEquals("Option 2", options.get(1).getLabel());
        assertEquals("opt2", options.get(1).getValue());
    }

    @Test
    void testChildListWithMissingProperties() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/options/option1",
                "label", "Option 1");
        context.create().resource(TEST_RESOURCE_PATH + "/options/option2",
                "value", "opt2");

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(AddToCartOptModelImpl.class);

        List<AddToCartOptModelImpl.Option> options = model.getOptions();
        assertNotNull(options);
        assertEquals(2, options.size());
        assertEquals("Option 1", options.get(0).getLabel());
        assertNull(options.get(0).getValue());
        assertNull(options.get(1).getLabel());
        assertEquals("opt2", options.get(1).getValue());
    }

    @Test
    void testGetExportedType() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(AddToCartOptModelImpl.class);

        assertEquals(RESOURCE_TYPE, model.getExportedType());
    }

    @Test
    void testDefensiveCopyOfOptionsList() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/options/option1",
                "label", "Option 1",
                "value", "opt1");

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(AddToCartOptModelImpl.class);

        List<AddToCartOptModelImpl.Option> options1 = model.getOptions();
        List<AddToCartOptModelImpl.Option> options2 = model.getOptions();

        assertEquals(options1.size(), options2.size());
        assertEquals(options1.get(0).getLabel(), options2.get(0).getLabel());
        assertEquals(options1.get(0).getValue(), options2.get(0).getValue());
    }
}