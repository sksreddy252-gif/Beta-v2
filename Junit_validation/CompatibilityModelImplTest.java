package com.example.core.models.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.example.core.models.impl.CompatibilityModelImpl;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.aem.junit5.ResourceResolverType;

@ExtendWith(AemContextExtension.class)
class CompatibilityModelImplTest {

    private static final String TEST_RESOURCE_PATH = "/content/test/compatibility";
    private static final String RESOURCE_TYPE = "example/components/compatibility";
    // Local constant placeholders for ApplicationConstants
    private static final String PN_TITLE = "title";
    private static final String PN_DESCRIPTION = "description";
    private static final String CHILD_NODE_NAME = "compatibilitycards";

    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);
    private CompatibilityModelImpl compatibilityModel;

    @BeforeEach
    void setUp() {
        context.addModelsForClasses(CompatibilityModelImpl.class);
    }

    @Test
    void testValueMapValueGetters() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE,
                PN_TITLE, "Sample Title",
                PN_DESCRIPTION, "Sample Description");
        context.currentResource(TEST_RESOURCE_PATH);
        compatibilityModel = context.request().adaptTo(CompatibilityModelImpl.class);

        assertNotNull(compatibilityModel);
        assertEquals("Sample Title", compatibilityModel.getTitle());
        assertEquals("Sample Description", compatibilityModel.getDescription());
    }

    @Test
    void testEmptyCompatibilityCardsList() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.currentResource(TEST_RESOURCE_PATH);
        compatibilityModel = context.request().adaptTo(CompatibilityModelImpl.class);

        List<?> cards = compatibilityModel.getCompatibilitycardsList();
        assertNotNull(cards);
        assertTrue(cards.isEmpty());
    }

    @Test
    void testCompatibilityCardsListWithItems() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/" + CHILD_NODE_NAME + "/item1",
                PN_TITLE, "Card 1 Title",
                PN_DESCRIPTION, "Card 1 Description");
        context.create().resource(TEST_RESOURCE_PATH + "/" + CHILD_NODE_NAME + "/item2",
                PN_TITLE, "Card 2 Title",
                PN_DESCRIPTION, "Card 2 Description");

        context.currentResource(TEST_RESOURCE_PATH);
        compatibilityModel = context.request().adaptTo(CompatibilityModelImpl.class);

        List<?> cards = compatibilityModel.getCompatibilitycardsList();
        assertNotNull(cards);
        assertEquals(2, cards.size());
        // Assuming card objects have getTitle and getDescription
        assertEquals("Card 1 Title", cards.get(0).getTitle());
        assertEquals("Card 1 Description", cards.get(0).getDescription());
        assertEquals("Card 2 Title", cards.get(1).getTitle());
        assertEquals("Card 2 Description", cards.get(1).getDescription());
    }

    @Test
    void testCompatibilityCardsListWithMissingProperties() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/" + CHILD_NODE_NAME + "/item1",
                PN_TITLE, "Card 1 Title");
        // Missing description

        context.currentResource(TEST_RESOURCE_PATH);
        compatibilityModel = context.request().adaptTo(CompatibilityModelImpl.class);

        List<?> cards = compatibilityModel.getCompatibilitycardsList();
        assertNotNull(cards);
        assertEquals(1, cards.size());
        assertEquals("Card 1 Title", cards.get(0).getTitle());
        assertNull(cards.get(0).getDescription());
    }

    @Test
    void testGetExportedType() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.currentResource(TEST_RESOURCE_PATH);
        compatibilityModel = context.request().adaptTo(CompatibilityModelImpl.class);

        assertEquals(RESOURCE_TYPE, compatibilityModel.getExportedType());
    }

    @Test
    void testDefensiveCopyOfList() {
        context.create().resource(TEST_RESOURCE_PATH,
                "sling:resourceType", RESOURCE_TYPE);
        context.create().resource(TEST_RESOURCE_PATH + "/" + CHILD_NODE_NAME + "/item1",
                PN_TITLE, "Card 1 Title",
                PN_DESCRIPTION, "Card 1 Description");

        context.currentResource(TEST_RESOURCE_PATH);
        compatibilityModel = context.request().adaptTo(CompatibilityModelImpl.class);

        List<?> firstCall = compatibilityModel.getCompatibilitycardsList();
        List<?> secondCall = compatibilityModel.getCompatibilitycardsList();

        assertEquals(firstCall.size(), secondCall.size());
        assertEquals(firstCall.get(0).getTitle(), secondCall.get(0).getTitle());
        assertNotSame(firstCall, secondCall);
    }
}