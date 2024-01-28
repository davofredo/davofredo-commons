package com.davofredo.util.map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MapScannerTest {

    private MapScanner mapScanner;

    @BeforeEach
    void beforeAll() {
        List<String> phoneNumbers = Arrays.asList("4665435677","3455677889","8776554434");
        var testMap = new HashMap<String, Object>();
        testMap.put("phoneNumbers", phoneNumbers);
        testMap.put("person", initPersonMap());
        testMap.put("content", "person");
        mapScanner = new MapScanner(testMap);
    }

    Map<String, Object> initPersonMap() {
        var skill1 = new HashMap<>();
        skill1.put("name", "Jump high");
        skill1.put("level", "pro");
        var skill2 = new HashMap<>();
        skill2.put("name", "Run fast");
        skill2.put("level", "intermediate");
        var skill3 = new HashMap<>();
        skill3.put("name", "Eat a lot");
        skill3.put("level", "rookie");
        var skillList = Arrays.asList(skill1, skill2, skill3);
        var address = new HashMap<>();
        address.put("street", "5th Avenue");
        address.put("number", "525");
        var person = new HashMap<String, Object>();
        person.put("firstName", "Aidan");
        person.put("lastName", "Proud");
        person.put("address", address);
        person.put("skills", skillList);
        return person;
    }

    @Test
    void testAddValueToRoot() {
        mapScanner.set("status", "active");
        // Confirm that initial values are still present
        verifyOriginalFieldsRemainIntact();
        // Verify no invalid fields are detected as false positives
        Assertions.assertFalse(mapScanner.fieldExists("invalidFieldName"));
        Assertions.assertFalse(mapScanner.fieldExists("person.invalidFieldName"));
        // Verify new value
        Assertions.assertTrue(mapScanner.fieldExists("status"));
        Assertions.assertEquals("active", mapScanner.get("status"));
    }

    @Test
    void testCheckForList_trueAndHasIndex() {
        MapAttribute attribute = new MapAttribute("myList[4].myItem");
        Assertions.assertTrue(mapScanner.checkForList(attribute));
        Assertions.assertEquals(4, attribute.getIndex());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "myList[]" // Has no index
    })
    void testCheckForList_true(String fieldName) {
        MapAttribute attribute = new MapAttribute(fieldName);
        Assertions.assertTrue(mapScanner.checkForList(attribute));
        Assertions.assertNull(attribute.getIndex());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "myList.myItem", // No square brackets
            "myList][.myItem", // Wrong square brackets
            "myList[5a].myItem" // Wrong number format
    })
    void testCheckForList_false(String fieldName) {
        MapAttribute attribute = new MapAttribute(fieldName);
        Assertions.assertFalse(mapScanner.checkForList(attribute));
        Assertions.assertNull(attribute.getIndex());
    }

    @Test
    void testToAttribute_listWithIndex() {
        MapAttribute attribute = mapScanner.toAttribute("myList[4].myItem");
        Assertions.assertEquals(MapAttribute.TYPE_LIST, attribute.getType());
        Assertions.assertEquals(4, attribute.getIndex());
        Assertions.assertEquals("myItem", attribute.getRemainingPath());
    }

    @Test
    void testToAttribute_map() {
        MapAttribute attribute = mapScanner.toAttribute("myMap.myItem");
        Assertions.assertEquals(MapAttribute.TYPE_MAP, attribute.getType());
        Assertions.assertNull(attribute.getIndex());
        Assertions.assertEquals("myItem", attribute.getRemainingPath());
    }

    @Test
    void testToAttribute_object() {
        MapAttribute attribute = mapScanner.toAttribute("myObject");
        Assertions.assertEquals(MapAttribute.TYPE_OBJECT, attribute.getType());
        Assertions.assertNull(attribute.getIndex());
        Assertions.assertNull(attribute.getRemainingPath());
    }

    @Test
    void testGet_fromRootAttribute() {
        Object value = mapScanner.get("content");
        Assertions.assertEquals("person", value);
    }

    @Test
    void testGet_fromInnerAttribute() {
        Object value = mapScanner.get("person.address.street");
        Assertions.assertEquals("5th Avenue", value);
    }

    @Test
    void testGet_fromListPosition() {
        Object value = mapScanner.get("phoneNumbers[1]");
        Assertions.assertEquals("3455677889", value);
    }

    @Test
    void testGet_fromOutOfBoundsListPosition() {
        Assertions.assertNull(mapScanner.get("phoneNumbers[3]"));
    }

    @Test
    void testGet_fieldFromNullListItem() {
        mapScanner.set("person.skillList[3]", null);
        Assertions.assertNull(mapScanner.get("person.skillList[3].name"));
    }

    @Test
    void testGet_fromListItem() {
        Object value = mapScanner.get("person.skills[2].level");
        Assertions.assertEquals("rookie", value);
    }

    @Test
    void testGet_bringFullList() {
        List<?> skillList = mapScanner.get("person.skills[]", List.class);
        Assertions.assertNotNull(skillList);
        Assertions.assertEquals(3, skillList.size());
        skillList.forEach(skill -> Assertions.assertInstanceOf(Map.class, skill));
    }

    @Test
    void testGet_fromNullListItem() {
        mapScanner.set("phoneNumbers[3]", null);
        Assertions.assertNull(mapScanner.get("phoneNumbers[3]"));
    }

    @Test
    void testGet_providingNullType() {
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> mapScanner.get("phoneNumbers[0]", null));
        Assertions.assertEquals("\"valueType\" argument is mandatory.", e.getMessage());
    }

    @Test
    void testSet_intoListWithMissingName() {
        mapScanner.set("person.[0].name", "mock name");
        Assertions.assertEquals("mock name", mapScanner.get("person.[0].name"));
        Assertions.assertInstanceOf(Map.class, mapScanner.get("person.[0]"));
        Map<?, ?> personMap = mapScanner.get("person", Map.class);
        // "[0]" cannot be considered a list, because it lacks of name, so it is handled as a field name (map key)
        Assertions.assertTrue(personMap.containsKey("[0]"));
    }

    @Test
    void testGet_nullValue() {
        Assertions.assertNull(mapScanner.get("person.emptyField", String.class));
    }

    @Test
    void testGet_classCastExceptionDueToWrongTypeRequest() {
        TypeCastException e = Assertions.assertThrows(TypeCastException.class,
                () -> mapScanner.get("person.firstName", Integer.class));
        Assertions.assertEquals("Class java.lang.Integer is not assignable from java.lang.String", e.getMessage());
    }

    @Test
    void testGet_classCastExceptionDueToLookingForSubMapFieldIntoNonMapListItem() {
        TypeCastException e = Assertions.assertThrows(TypeCastException.class,
                () -> mapScanner.get("phoneNumbers[1].lada", Integer.class));
        Assertions.assertEquals("Field \"phoneNumbers[1]\" was expected to be an instance of java.util.Map, but got java.lang.String when trying to read a value from \"phoneNumbers[1].lada\"", e.getMessage());
    }

    @Test
    void testGet_lookingForListItemAttributeWithMissingListIndex() {
        IllegalStateException e = Assertions.assertThrows(IllegalStateException.class,
                () -> mapScanner.get("person.skills[].name"));
        Assertions.assertEquals("Field \"skills\" in path \"skills[].name\" is an array, but the index was not specified. Cannot continue through path without an index", e.getMessage());
    }

    @Test
    void testSet_createMapperFromScratchAndAddRootField() {
        MapScanner mapScanner1 = new MapScanner();
        mapScanner1.set("myField", "myValue");
        Assertions.assertTrue(mapScanner1.fieldExists("myField"));
        Assertions.assertEquals("myValue", mapScanner1.get("myField"));
    }

    @Test
    void testSet_addRootFieldInExistingMap() {
        mapScanner.set("myField", "myValue");
        verifyOriginalFieldsRemainIntact();
        Assertions.assertTrue(mapScanner.fieldExists("myField"));
        Assertions.assertEquals("myValue", mapScanner.get("myField"));
    }

    @Test
    void testSet_addInnerFieldInExistingMap() {
        mapScanner.set("person.birthDate", "2002-06-11");
        verifyOriginalFieldsRemainIntact();
        Assertions.assertTrue(mapScanner.fieldExists("person.birthDate"));
        Assertions.assertEquals("2002-06-11", mapScanner.get("person.birthDate"));
    }

    @Test
    void testSet_addListItemInExistingMap() {
        mapScanner.set("phoneNumbers[]", "8965236472");
        verifyOriginalFieldsRemainIntact();
        Assertions.assertTrue(mapScanner.fieldExists("phoneNumbers[3]"));
        Assertions.assertEquals("8965236472", mapScanner.get("phoneNumbers[3]"));
    }

    @Test
    void testSet_incorrectlyAssumeMap() {
        TypeCastException e = Assertions.assertThrows(TypeCastException.class,
                () -> mapScanner.set("phoneNumbers.lada", "999"));
        Assertions.assertEquals("Field \"phoneNumbers\" was expected to be an instance of java.util.Map, but got java.util.ArrayList when trying to write a value into \"phoneNumbers.lada\"", e.getMessage());
    }

    @Test
    void testSet_incorrectlyAssumeList() {
        TypeCastException e = Assertions.assertThrows(TypeCastException.class,
                () -> mapScanner.set("person[1].firstName", "999"));
        Assertions.assertEquals("Field \"person\" was expected to be an instance of java.util.List, but got java.util.HashMap when trying to write a value into \"person[1].firstName\"", e.getMessage());
    }

    @Test
    void testSet_editListItemField() {
        mapScanner.set("person.skills[2].level", "master");
        Assertions.assertEquals("Eat a lot", mapScanner.get("person.skills[2].name"));
        Assertions.assertEquals("master", mapScanner.get("person.skills[2].level"));
    }

    @Test
    void testSet_addListItemField() {
        mapScanner.getSubScanner("person.skills[3]")
                .set("name", "sleep")
                .set("level", "god");
        verifyOriginalFieldsRemainIntact();
        Assertions.assertEquals(4, mapScanner.get("person.skills", List.class).size());
        Assertions.assertEquals("sleep", mapScanner.get("person.skills[3].name"));
        Assertions.assertEquals("god", mapScanner.get("person.skills[3].level"));
    }

    @Test
    void testSet_addListItemField_fillItemsInTheMiddle() {
        mapScanner.set("person.skills[9].name", "sleep");
        mapScanner.set("person.skills[9].level", "god");
        verifyOriginalFieldsRemainIntact();
        Assertions.assertEquals(10, mapScanner.get("person.skills", List.class).size());
        Assertions.assertNull(mapScanner.get("person.skills[3]"));
        Assertions.assertNull(mapScanner.get("person.skills[4]"));
        Assertions.assertNull(mapScanner.get("person.skills[5]"));
        Assertions.assertNull(mapScanner.get("person.skills[6]"));
        Assertions.assertNull(mapScanner.get("person.skills[7]"));
        Assertions.assertNull(mapScanner.get("person.skills[8]"));
        Assertions.assertNotNull(mapScanner.get("person.skills[9]"));
        Assertions.assertEquals("sleep", mapScanner.get("person.skills[9].name"));
        Assertions.assertEquals("god", mapScanner.get("person.skills[9].level"));
    }

    @Test
    void testFieldExists_lookForEntireList() {
        Assertions.assertTrue(mapScanner.fieldExists("phoneNumbers[]"));
    }

    @Test
    void testFieldExists_lookForListItemField_omitIndex() {
        IllegalStateException e = Assertions.assertThrows(IllegalStateException.class, () -> mapScanner.fieldExists("person.skills[].name"));
        Assertions.assertEquals("Field \"skills\" in path \"skills[].name\" is an array, but the index was not specified. Cannot continue through path without an index", e.getMessage());
    }

    @Test
    void testFieldExists_incorrectlyAssumeList() {
        TypeCastException e = Assertions.assertThrows(TypeCastException.class, () -> mapScanner.fieldExists("person[1].firstName"));
        Assertions.assertEquals("Field \"person\" was expected to be an instance of java.util.List, but got java.util.HashMap when trying to read a value from \"person[1].firstName\"", e.getMessage());
    }

    @Test
    void testFieldExists_incorrectlyAssumeMapAttributeIsMap() {
        TypeCastException e = Assertions.assertThrows(TypeCastException.class, () -> mapScanner.fieldExists("person.firstName.name"));
        Assertions.assertEquals("Field \"firstName\" was expected to be an instance of java.util.Map, but got java.lang.String when trying to read a value from \"firstName.name\"", e.getMessage());
    }

    @Test
    void testFieldExists_listItemIndexIsOutOfBounds() {
        Assertions.assertFalse(mapScanner.fieldExists("phoneNumbers[6]"));
    }

    @Test
    void testFieldExists_listItemIsNull() {
        mapScanner.set("person.skills[3]", null);
        Assertions.assertFalse(mapScanner.fieldExists("person.skills[3].name"));
    }

    @Test
    void testFieldExists_lookForListItemField_resultIsTrue() {
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[2].name"));
    }

    @Test
    void testFieldExists_lookForListItemField_resultIsFalse() {
        Assertions.assertFalse(mapScanner.fieldExists("person.skills[2].phoneNumber"));
    }

    @Test
    void testFieldExists_lookForListItemField_butListItemIsNotAMap() {
        TypeCastException e = Assertions.assertThrows(TypeCastException.class, () -> mapScanner.fieldExists("phoneNumbers[0].lada"));
        Assertions.assertEquals("Field \"phoneNumbers[0]\" was expected to be an instance of java.util.Map, but got java.lang.String when trying to read a value from \"phoneNumbers[0].lada\"", e.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testToMap() {
        var map = mapScanner.toMap();
        Assertions.assertNotNull(map);
        Assertions.assertFalse(map.isEmpty());
        Assertions.assertTrue(map.containsKey("person"));
        Assertions.assertNotNull(map.get("person"));
        Assertions.assertInstanceOf(Map.class, map.get("person"));
        Assertions.assertTrue(((Map<String, Object>) map.get("person")).containsKey("address"));
    }

    @Test
    void testGetSubScanner() {
        var subScanner = mapScanner.getSubScanner("person");
        Assertions.assertNotNull(subScanner);
        Assertions.assertTrue(subScanner.fieldExists("address"));
        Assertions.assertEquals("5th Avenue", subScanner.get("address.street"));
    }

    @Test
    void testRemoveFromMissingKey() {
        Assertions.assertNull(mapScanner.get("person.children"));
        mapScanner.remove("person.children[0].skills[0].name");
        // Children didn't exist and shouldn't be created from a remove action
        Assertions.assertNull(mapScanner.get("person.children"));
    }

    @Test
    void testRemoveFromRootMap() {
        Assertions.assertNotNull(mapScanner.get("content"));
        mapScanner.remove("content");
        Assertions.assertNull(mapScanner.get("content"));
    }

    @Test
    void testRemoveFromSubMap() {
        Assertions.assertNotNull(mapScanner.get("person.firstName"));
        mapScanner.remove("person.firstName");
        Assertions.assertNull(mapScanner.get("person.firstName"));
    }

    @Test
    void testRemoveFromNullList() {
        mapScanner.set("person.children", null);
        Assertions.assertNull(mapScanner.get("person.children[0].firstName"));
        mapScanner.remove("person.children[0].firstName");
        Assertions.assertNull(mapScanner.get("person.children[0].firstName"));
    }

    @Test
    void testRemoveFromOutOfBoundsIndex() {
        Assertions.assertEquals(3, mapScanner.get("person.skills", List.class).size());
        mapScanner.remove("person.skills[4].name");
        Assertions.assertNull(mapScanner.get("person.skills[4].name"));
        Assertions.assertEquals(3, mapScanner.get("person.skills", List.class).size());
    }

    @Test
    void testRemoveListItem() {
        Assertions.assertEquals(3, mapScanner.get("phoneNumbers", List.class).size());
        mapScanner.remove("phoneNumbers[1]");
        Assertions.assertEquals(2, mapScanner.get("phoneNumbers", List.class).size());
        Assertions.assertEquals("4665435677", mapScanner.get("phoneNumbers[0]"));
        Assertions.assertEquals("8776554434", mapScanner.get("phoneNumbers[1]"));
    }

    @Test
    void testRemoveFieldFromListItem() {
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0].name"));
        mapScanner.remove("person.skills[0].name");
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0]"));
        Assertions.assertFalse(mapScanner.fieldExists("person.skills[0].name"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0].level"));
    }

    @Test
    void testRemoveAllTheList() {
        Assertions.assertTrue(mapScanner.fieldExists("phoneNumbers[]"));
        mapScanner.remove("phoneNumbers[]");
        Assertions.assertFalse(mapScanner.fieldExists("phoneNumbers[]"));
    }

    @Test
    void testRemoveFieldFromAllTheListItems() {
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0].name"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[1].name"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[2].name"));
        mapScanner.remove("person.skills[].name");
        Assertions.assertTrue(mapScanner.fieldExists("person.skills"));
        Assertions.assertFalse(mapScanner.fieldExists("person.skills[0].name"));
        Assertions.assertFalse(mapScanner.fieldExists("person.skills[1].name"));
        Assertions.assertFalse(mapScanner.fieldExists("person.skills[2].name"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0].level"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[1].level"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[2].level"));
    }

    @Test
    void testRemove2FieldsFromListItem() {
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0].name"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0].level"));
        mapScanner.getSubScanner("person.skills[0]").remove("name").remove("level");
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0]"));
        Assertions.assertFalse(mapScanner.fieldExists("person.skills[0].name"));
        Assertions.assertFalse(mapScanner.fieldExists("person.skills[0].level"));
    }

    private void verifyOriginalFieldsRemainIntact() {
        // Check content field
        Assertions.assertEquals("person", mapScanner.get("content"));
        Assertions.assertNotNull(mapScanner.get("phoneNumbers"));
        // Check phone numbers field
        Assertions.assertTrue(mapScanner.get("phoneNumbers", List.class).contains("4665435677"));
        Assertions.assertTrue(mapScanner.get("phoneNumbers", List.class).contains("3455677889"));
        Assertions.assertTrue(mapScanner.get("phoneNumbers", List.class).contains("8776554434"));
        // Check person field
        Assertions.assertNotNull(mapScanner.get("person"));
        Assertions.assertEquals("Aidan", mapScanner.get("person.firstName"));
        Assertions.assertEquals("Proud", mapScanner.get("person.lastName"));
        Assertions.assertEquals("5th Avenue", mapScanner.get("person.address.street"));
        Assertions.assertEquals("525", mapScanner.get("person.address.number"));
        Assertions.assertNotNull(mapScanner.get("person.skills"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[0]"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[1]"));
        Assertions.assertTrue(mapScanner.fieldExists("person.skills[2]"));
        Assertions.assertNotNull(mapScanner.get("person.skills[0]"));
        Assertions.assertNotNull(mapScanner.get("person.skills[1]"));
        Assertions.assertNotNull(mapScanner.get("person.skills[2]"));
        Assertions.assertEquals("Jump high", mapScanner.get("person.skills[0].name"));
        Assertions.assertEquals("pro", mapScanner.get("person.skills[0].level"));
        Assertions.assertEquals("Run fast", mapScanner.get("person.skills[1].name"));
        Assertions.assertEquals("intermediate", mapScanner.get("person.skills[1].level"));
        Assertions.assertEquals("Eat a lot", mapScanner.get("person.skills[2].name"));
        Assertions.assertEquals("rookie", mapScanner.get("person.skills[2].level"));
    }

}
