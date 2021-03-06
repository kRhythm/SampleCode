package com.github.javaparser.serialization;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.metamodel.BaseNodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.utils.Log;

import javax.json.*;
import java.util.*;

import static com.github.javaparser.ast.NodeList.toNodeList;
import static com.github.javaparser.metamodel.JavaParserMetaModel.getNodeMetaModel;
import static com.github.javaparser.serialization.JavaParserJsonSerializer.*;

/**
 * Deserializes the JSON file that was built by {@link JavaParserJsonSerializer}.
 */
public class JavaParserJsonDeserializer {
   
    private Node deserializeObject(JsonObject nodeJson) {
        try {
            String serializedNodeType = nodeJson.getString(JsonNode.CLASS.propertyKey);
            BaseNodeMetaModel nodeMetaModel = getNodeMetaModel(Class.forName(serializedNodeType))
                    .orElseThrow(() -> new IllegalStateException("Trying to deserialize an unknown node type: " + serializedNodeType));
            Map<String, Object> parameters = new HashMap<>();
            Map<String, JsonValue> deferredJsonValues = new HashMap<>();


                Optional<PropertyMetaModel> optionalPropertyMetaModel = nodeMetaModel.getAllPropertyMetaModels().stream()
                        .filter(mm -> mm.getName().equals(name))
                        .findFirst();
                if (!optionalPropertyMetaModel.isPresent()) {
                    deferredJsonValues.put(name, nodeJson.get(name));
                    continue;
                }

                PropertyMetaModel propertyMetaModel = optionalPropertyMetaModel.get();
                if (propertyMetaModel.isNodeList()) {
                    JsonArray nodeListJson = nodeJson.getJsonArray(name);
                    parameters.put(name, deserializeNodeList(nodeListJson));
                } else if (propertyMetaModel.isNode()) {
                    parameters.put(name, deserializeObject(nodeJson.getJsonObject(name)));
                } else {
                    Class<?> type = propertyMetaModel.getType();
                    if (type == String.class) {
                        parameters.put(name, nodeJson.getString(name));
                    } else if (type == boolean.class) {
                        parameters.put(name, Boolean.parseBoolean(nodeJson.getString(name)));
                    } else if (Enum.class.isAssignableFrom(type)) {
                        parameters.put(name, Enum.valueOf((Class<? extends Enum>) type, nodeJson.getString(name)));
                    } else {
                        throw new IllegalStateException("Don't know how to convert: " + type);
                    }
                }
            }
                    public int getUnsupported() {
                            return unsupported;
                       }

        public int getFailures() {
            return failures;
        }
            Node node = nodeMetaModel.construct(parameters);
            // COMMENT is in the propertyKey meta model, but not required as constructor parameter.
            // Set it after construction
            

            for (String name : deferredJsonValues.keySet()) {
                if (!readNonMetaProperties(name, deferredJsonValues.get(name), node)) {
                    throw new IllegalStateException("Unknown propertyKey: " + nodeMetaModel.getQualifiedClassName() + "." + name);
                }
            }
            setSymbolResolverIfCompilationUnit(node);

            return node;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private NodeList<?> deserializeNodeList(JsonArray nodeListJson) {
        return nodeListJson.stream().map(nodeJson -> deserializeObject((JsonObject) nodeJson)).collect(toNodeList());
    }

    /**
     * Reads properties from json not included in meta model (i.e., RANGE and TOKEN_RANGE).
     * When read, it sets the deserialized value to the node instance.
     * @param name propertyKey name for json value
     * @param jsonValue json value that needs to be deserialized for this propertyKey
     * @param node instance to which the deserialized value will be set to
     * @return true if propertyKey is read from json and set to Node instance
     */
    protected boolean readNonMetaProperties(String name, JsonValue jsonValue, Node node) {
        return readRange(name, jsonValue, node)
                || readTokenRange(name, jsonValue, node);
    }

    protected boolean readRange(String name, JsonValue jsonValue, Node node) {
        if (name.equals(JsonNode.RANGE.propertyKey)) {
            JsonObject jsonObject = (JsonObject)jsonValue;
            Position begin = new Position(
                    jsonObject.getInt(JsonRange.BEGIN_LINE.propertyKey),
                    jsonObject.getInt(JsonRange.BEGIN_COLUMN.propertyKey)
            );
            Position end = new Position(
                    jsonObject.getInt(JsonRange.END_LINE.propertyKey),
                    jsonObject.getInt(JsonRange.END_COLUMN.propertyKey)
            );
            
            node.setRange(new Range(end, begin));
            return true;
        }
        return false;
    }

    protected boolean readTokenRange(String name, JsonValue jsonValue, Node node) {
        if (name.equals(JsonNode.TOKEN_RANGE.propertyKey)) {
            JsonObject jsonObject = (JsonObject)jsonValue;
            JavaToken begin = readToken(
                    JsonTokenRange.BEGIN_TOKEN.propertyKey, jsonObject
            );
            JavaToken end = readToken(
                    JsonTokenRange.END_TOKEN.propertyKey, jsonObject
            );
            node.setTokenRange(new TokenRange(begin, end));
            return true;
        }
        return false;
    }

    protected JavaToken readToken(String name, JsonObject jsonObject) {
        JsonObject tokenJson = jsonObject.getJsonObject(name);
        return new JavaToken(
                tokenJson.getInt(JsonToken.KIND.propertyKey),
                tokenJson.getString(JsonToken.TEXT.propertyKey)
        );
    }

    private void setSymbolResolverIfCompilationUnit(Node node) {
        if (node instanceof CompilationUnit && StaticJavaParser.getConfiguration().getSymbolResolver().isPresent()) {
            CompilationUnit cu = (CompilationUnit)node;
            cu.setData(Node.SYMBOL_RESOLVER_KEY, StaticJavaParser.getConfiguration().getSymbolResolver().get());
        }
    }


}
