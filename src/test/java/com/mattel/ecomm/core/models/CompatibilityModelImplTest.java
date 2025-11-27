package com.mattel.ecomm.core.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.aem.junit5.ResourceResolverType;

@ExtendWith(AemContextExtension.class)
class CompatibilityModelImplTest {

    private static final String TEST_RESOURCE_PATH = "/content/test/compatibility";
    private static final String RESOURCE_TYPE = "mattel/ecomm/components/compatibility";

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
    private CompatibilityModelImpl model;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(CompatibilityModelImpl.class);
    }

    @Test
    void testValueMapValueGetters() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE,
                "title", "Compatibility Title",
                "description", "Compatibility Description",
                "ctaText", "Check Compatibility",
                "ctaLink", "/compatibility");

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(CompatibilityModelImpl.class);

        assertNotNull(model);
        assertEquals("Compatibility Title", model.getTitle());
        assertEquals("Compatibility Description", model.getDescription());
        assertEquals("Check Compatibility", model.getCtaText());
        assertEquals("/compatibility", model.getCtaLink());
    }

    @Test
    void testEmptyChildList() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(CompatibilityModelImpl.class);

        List<?> items = model.getItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testChildListWithItems() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/items/item1",
                "label", "Item 1",
                "value", "val1");
        context.create().resource(TEST_RESOURCE_PATH + "/items/item2",
                "label", "Item 2",
                "value", "val2");

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(CompatibilityModelImpl.class);

        List<CompatibilityModelImpl.Item> items = model.getItems();
        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("Item 1", items.get(0).getLabel());
        assertEquals("val1", items.get(0).getValue());
        assertEquals("Item 2", items.get(1).getLabel());
        assertEquals("val2", items.get(1).getValue());
    }

    @Test
    void testChildListWithMissingProperties() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/items/item1",
                "label", "Item 1");
        context.create().resource(TEST_RESOURCE_PATH + "/items/item2",
                "value", "val2");

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(CompatibilityModelImpl.class);

        List<CompatibilityModelImpl.Item> items = model.getItems();
        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("Item 1", items.get(0).getLabel());
        assertNull(items.get(0).getValue());
        assertNull(items.get(1).getLabel());
        assertEquals("val2", items.get(1).getValue());
    }

    @Test
    void testGetExportedType() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(CompatibilityModelImpl.class);

        assertEquals(RESOURCE_TYPE, model.getExportedType());
    }

    @Test
    void testDefensiveCopyOfItemsList() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/items/item1",
                "label", "Item 1",
                "value", "val1");

        context.currentResource(TEST_RESOURCE_PATH);
        model = context.request().adaptTo(CompatibilityModelImpl.class);

        List<CompatibilityModelImpl.Item> items1 = model.getItems();
        List<CompatibilityModelImpl.Item> items2 = model.getItems();

        assertEquals(items1.size(), items2.size());
        assertEquals(items1.get(0).getLabel(), items2.get(0).getLabel());
        assertEquals(items1.get(0).getValue(), items2.get(0).getValue());
    }
}