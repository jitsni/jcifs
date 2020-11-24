/**
 * Copyright 2020 Jitendra Kotamraju.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcifs.dcerpc.msrpc.eventing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * @author Jitendra Kotamraju
 */
abstract class BinXmlNode {

    static class BinXmlRoot {
        private BinXmlElement element;
        private BinXmlTemplate template;


        void addChild(BinXmlElement child) {
            assert this.element == null && this.template == null;
            this.element = child;
        }

        void addChild(BinXmlTemplate child) {
            assert this.element == null && this.template == null;
            this.template = child;
        }

        String xml() {
            return element != null ? element.xml() : template.xml();
        }
    }

    static class BinXmlElement {
        private final List<BinXmlElement> children;
        private final List<Attribute> attributeList;

        private String tag;
        private String text;
        private Substitution textSubstitution;

        BinXmlElement() {
            children = new ArrayList<>();
            attributeList = new ArrayList<>();
        }

        void addChild(BinXmlElement child) {
            children.add(child);
        }

        void setTag(String tag) {
            this.tag = tag;
        }

        void setText(String text) {
            this.text = text;
        }

        void setTextSubstitution(Substitution substitution) {
            this.textSubstitution = substitution;
        }

        void addAttribute(Attribute attribute) {
            attributeList.add(attribute);
        }

        String xml() {
            return xml(Collections.emptyList());
        }

        private String xml(List<BinXmlParser.ValueEntry> substitutions) {
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
                    for (BinXmlElement child : children) {
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

        private String attributeValue(List<BinXmlParser.ValueEntry> substitutions, Attribute attribute) {
            if (attribute.value != null) {
                return attribute.value;
            } else if (attribute.substitution != null) {
                Object value = substitutions.get(attribute.substitution.index).value;
                return Objects.toString(value);
            }

            return null;
        }

        private String text(List<BinXmlParser.ValueEntry> substitutions) {
            if (text != null) {
                return text;
            } else if (textSubstitution != null) {
                Object value = substitutions.get(textSubstitution.index).value;
                if (value instanceof BinXmlRoot) {
                    return ((BinXmlRoot) value).xml();
                }
                return Objects.toString(value);
            }

            return null;
        }

        @Override
        public String toString() {
            return "<"+tag+" "+attributeList+" >";
        }

    }

    static class BinXmlTemplate {
        private BinXmlElement element;
        private List<BinXmlParser.ValueEntry> substitutions;

        void setValues(List<BinXmlParser.ValueEntry> values) {
            this.substitutions = values;
        }

        void addChild(BinXmlElement child) {
            this.element = child;
        }

        String xml() {
            return element.xml(substitutions);
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
