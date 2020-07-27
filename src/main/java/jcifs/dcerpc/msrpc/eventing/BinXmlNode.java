package jcifs.dcerpc.msrpc.eventing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * @author Jitendra Kotamraju
 */
public class BinXmlNode {

    final List<BinXmlNode> children;

    BinXmlNode() {
        children = new ArrayList<>();
    }

    void addChild(BinXmlNode child) {
        children.add(child);
    }

    public String xml() {
        return xml(Collections.emptyList());
    }

    String xml(List<BinXmlParser.ValueEntry> substitutions) {
        StringBuilder sb = new StringBuilder();
        for(BinXmlNode node : children) {
            sb.append(node.xml(substitutions));
        }
        return sb.toString();
    }

    public static class BinXmlElement extends BinXmlNode {
        final List<Attribute> attributeList;

        String tag;
        String text;
        Substitution textSubstitution;

        BinXmlElement() {
            attributeList = new ArrayList<>();
        }

        void setTag(String tag) {
            this.tag = tag;
        }

        public void setText(String text) {
            this.text = text;
        }

        void setTextSubstitution(Substitution substitution) {
            this.textSubstitution = substitution;
        }

        public void addAttribute(Attribute attribute) {
            attributeList.add(attribute);
        }

        @Override
        String xml(List<BinXmlParser.ValueEntry> substitutions) {
            if (skipElement(substitutions,this)) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            sb.append(tag);
            if (!attributeList.isEmpty()) {
                for(Attribute attribute : attributeList) {
                    if (!skipAttribute(substitutions, attribute)) {
                        String value = attributeValue(substitutions, attribute);
                        sb.append(" ");
                        sb.append(attribute.name).append("=").append("\"").append(value).append("\"");
                    }
                }
            }

            String text = text(substitutions);
            if (children.isEmpty() && text == null) {
                sb.append("/>");
            } else {
                sb.append(">");
                if (!children.isEmpty()) {
                    for (BinXmlNode child : children) {
                        sb.append(child.xml(substitutions));
                    }
                }
                if (text != null) {
                    sb.append(text);
                }
                sb.append("</").append(tag).append(">");
            }

            return sb.toString();
        }

        private boolean skipElement(List<BinXmlParser.ValueEntry> substitutions, BinXmlElement element) {
            if (element.textSubstitution != null && element.textSubstitution.optional) {
                return substitutions.get(element.textSubstitution.index).valueByteLength == 0;
            }
            return false;
        }

        private boolean skipAttribute(List<BinXmlParser.ValueEntry> substitutions, Attribute attribute) {
            if (attribute.substitution != null && attribute.substitution.optional) {
                return substitutions.get(attribute.substitution.index).valueByteLength == 0;
            }
            return false;
        }

        String attributeValue(List<BinXmlParser.ValueEntry> substitutions, Attribute attribute) {
            if (attribute.value != null) {
                return attribute.value;
            } else if (attribute.substitution != null) {
                Object value = substitutions.get(attribute.substitution.index).value;
                return Objects.toString(value);
            }

            return null;
        }

        String text(List<BinXmlParser.ValueEntry> substitutions) {
            if (text != null) {
                return text;
            } else if (textSubstitution != null) {
                Object value = substitutions.get(textSubstitution.index).value;
                if (value instanceof BinXmlNode) {
                    return ((BinXmlNode) value).xml();
                }
                return Objects.toString(value);
            }

            return null;
        }

        public String toString() {
            return "<"+tag+" "+attributeList+" >";
        }

    }

    public static class BinXmlTemplate extends BinXmlNode {
        int templateInstanceDataOffset;
        List<BinXmlParser.ValueEntry> substitutions;

        void setValues(List<BinXmlParser.ValueEntry> values) {
            this.substitutions = values;
        }

        @Override
        public String xml() {
            return xml(Collections.emptyList());
        }

        @Override
        String xml(List<BinXmlParser.ValueEntry> substitutions) {
            StringBuilder sb = new StringBuilder();
            for(BinXmlNode node : this.children) {
                String xml = node.xml(this.substitutions);
                sb.append(xml);
            }
            return sb.toString();
        }
    }

    static class Substitution {
        final boolean optional;
        final int index;
        byte valueType;

        Substitution(boolean optional, int index) {
            this.optional = optional;
            this.index = index;
        }

        void setValueType(byte valueType) {
            this.valueType = valueType;
        }
    }

    static class Attribute {
        String name;
        String value;
        Substitution substitution;

        void setName(String name) {
            this.name = name;
        }

        void setValue(String value) {
            this.value = value;
        }

        void setSubstitution(Substitution substitution) {
            this.substitution = substitution;
        }

        @Override
        public String toString() {
            return name + "=" + (value == null ? substitution.toString() : value);
        }
    }
}
