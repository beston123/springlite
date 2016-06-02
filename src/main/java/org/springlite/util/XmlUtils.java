package org.springlite.util;

import org.w3c.dom.*;

/**
 * 〈一句话功能简述〉&lt;p&gt;
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 16/6/2
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public abstract class XmlUtils {

    /**
     * 打印节点信息
     * @param element
     * @param includeChildNodes
     * @return
     */
    public static String getElementText(Element element, boolean includeChildNodes){
        StringBuilder elementText = new StringBuilder();
        String tagName = element.getNodeName();
        elementText.append("< ").append(tagName);

        // element元素的所有属性构成的NamedNodeMap对象，需要对其进行判断
        NamedNodeMap map = element.getAttributes();

        // 如果存在属性，则打印属性
        if (null != map) {
            for (int i = 0; i < map.getLength(); i++)
            {
                // 获得该元素的每一个属性
                Attr attr = (Attr) map.item(i);

                // 属性名和属性值
                String attrName = attr.getName();
                String attrValue = attr.getValue();

                // 注意属性值需要加上引号，所以需要\转义
                elementText.append(" ").append(attrName).append("=\"").append(attrValue).append("\"");
            }
        }

        // 关闭标签名
        if(!element.hasChildNodes()){
            elementText.append(" />");
        }else{
            elementText.append(" >");
        }

        // 它的子节点
        if(includeChildNodes){
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                // 获取每一个child
                Node node = children.item(i);
                // 获取节点类型
                short nodeType = node.getNodeType();

                if (nodeType == Node.ELEMENT_NODE) {
                    // 如果是元素类型，则递归输出
                    String childElementText = getElementText((Element) node, includeChildNodes);
                    elementText.append("\t").append(childElementText).append("\n\r");
                } else if (nodeType == Node.TEXT_NODE) {
                    // 如果是文本类型，则输出节点值，及文本内容
                    System.out.print(node.getNodeValue());
                } else if (nodeType == Node.COMMENT_NODE) {
                    // 如果是注释，则输出注释
                    elementText.append("<!--");
                    Comment comment = (Comment) node;
                    // 注释内容
                    String data = comment.getData();
                    elementText.append(data);
                    elementText.append("-->").append("\n\r");
                }
            }
            // 所有内容处理完之后，输出，关闭根节点
            elementText.append("</").append(tagName).append(">");
        }
        return elementText.toString();
    }
}
