# Davofredo Commons
**Group Name:** com.davofredo  
**Artifact ID:** common-utils
______________________________
## List of Classes
* ``` Java
  com.davofredo.util.map.MapUtils
* ``` Java
  com.davofredo.util.map.MapScanner
_____________________________
## MapUtils
Provides utility methods to work with instances of 
```java.util.Map```.
### Methods
* ``` java
    public <K> Map<K, Object> cloneRecursively(Map<K, Object> from)

### Usage
Clone a map and all the sub-maps as well as all the lists contained in it.
Any values not assignable into ```java.util.Map``` or 
```java.util.List``` will be passed as reference, except for 
primitives which will be passed as value.
``` Java
var personMap = new LinkedHashMap<String, Object>();
personMap.put("address", new LinkedHashMap<String, Object>());
...
personMap.put("phoneNumbers", Arrays.asList(...));
personMap.put("id", 123);
personMap.put("birthDate", new LocalDate("1992-02-26"));

var personClone = MapUtils.cloneRecursively(sourceMap);
// personMap was cloned and the clone was assigned to personClone
// personClone now contains clones of "address" and "phoneNumbers" fields
// "id" field was passed as value and 
// "birthDate" was passed as reference
```
________________
## MapScanner
Allows to read and write values into a tree of instances of 
```java.util.Map``` at any tree level, providing an experience
similar to edit a JSON.
### Methods
* ``` java
  public Object get(String fieldName)
  ```
* ``` java
  public <T> T get(String fieldName, Class<T> valueType)
  ```
* ``` java
  public MapScanner set(String fieldName, Object value)
  ```
* ``` java
  public MapScanner remove(String fieldName)
  ```
* ``` java
  public boolean fieldExists(String fieldName)
  ```
* ``` java
  public Map<String, Object> toMap()
  ```
* ``` java
  public MapScanner getSubScanner(String fieldName)
  ```
### Usage
Read values from map, sub-map and list nodes
``` java
// Read field from root map
Object status = responseScanner.get("status");

// Read field from sub-map and specify expected type
String personName = responseScanner.get("person.firstName", String.class);

// Read entire list;
List phoneNumbers = responseScanner.get("person.phoneNumbers", List.class);

// Read list item
var secondPhone = responseScanner.get("person.phoneNumbers[1]", String.class);

// Read field from list item
var skillName = responseScanner.get("person.skills[0].name", String.class);

// No NullPointerException if the field or any of it's parent nodes is missing
var missingValue = responseScanner.get("person.missingField.missingValue"); // missingValue = null

// No ArrayIndexOutOfBoundsException when trying to reach an index out of bounds
var sixthItem = responseScanner.get("person.twoItemsList[5]"); // sixthItem = null
```
Write values into map, sub-map and list nodes
``` java
// Write field into root map
responseScanner.set("status", "SUCCESS");

// Write field into sub-map
responseScanner.set("person.firstName", "John");

// Add list item;
responseScanner.set("person.phoneNumbers[]", "1234567890");

// Edit list item
responseScanner.set("person.phoneNumbers[1]", "1234567890");

// Write field into list item
responseScanner.set("person.skills[0].name", "Management");

// No NullPointerException if the field or any of it's parent nodes is missing
responseScanner.set("person.missingField.missingValue", null); // person.missingField.missingValue = null

// No ArrayIndexOutOfBoundsException when trying to edit an item out of bounds
responseScanner.set("person.twoItemsList[5]", true); // person.twoItemsList.size() = 6 ... [true, false, null, null, null, true]

// Builder-like setter
responseScanner.set("message", "Success")
    .set("status", "SUCCESS")
    .set("timestamp", timestamp)
    .set("person", personMap);
```
Remove nodes from a map, sub-map and list nodes
``` java
// Remove field from sub-map (The key will be removed from the sub-map)
responseScanner.remove("person.firstName");

// Remove list item
responseScanner.remove("person.phoneNumbers[1]");

// Remove a field from every list item
responseScanner.remove("person.skills[].level");

// Remove multiple fields
responseScanner.remove("message")
    .remove("status")
    .remove("timestamp");
```
Check if a field is present
``` java
// Review if field exists in sub-map
var isFirstNamePresent = responseScanner.fieldExists("person.firstName");

// Review if list item exists
var isSecondNumPresent = responseScanner.fieldExists("person.phoneNumbers[1]");
```
Export as a Map
``` java
// Export to an instance of java.util.Map
Map<String, Object> exportMap = responseScanner.toMap();
```
Export nodes as sub-scanner
``` java
// Export person MapScanner
MapScanner personScanner = responseScanner.getSubScanner("person");

// Alternative way to edit nodes
responseScanner.subScanner("person")
    .set("firstName", "John")
    .set("lastName", "Smith");
// Previous statement is equivalent to:
responseScanner
    .set("person.firstName", "John")
    .set("person.lastName", "Smith");
```