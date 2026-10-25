package com.gooci.console.support;

import com.gooci.console.common.BusinessException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @ClassName StaffSheetSupport
 * @Description 员工导入表的简易 xlsx 读写
 * @author GooCi
 * @date 2026-03-24
 */
public final class StaffSheetSupport {

    private StaffSheetSupport() {
    }

    public static List<List<String>> read(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new BusinessException("请上传 Excel 文件");
        }
        Map<String, byte[]> parts = unzip(bytes);
        List<String> shared = readShared(parts.get("xl/sharedStrings.xml"));
        byte[] sheet = firstSheet(parts);
        if (sheet == null) {
            throw new BusinessException("Excel 中没有工作表");
        }
        return readSheet(sheet, shared);
    }

    public static byte[] writeTemplate(List<String> headers) {
        List<String> cols = headers == null || headers.isEmpty() ? List.of("手机号码", "人员名称") : headers;
        StringBuilder sheet = new StringBuilder();
        sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sheet.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sheet.append("<sheetData><row r=\"1\">");
        for (int i = 0; i < cols.size(); i++) {
            sheet.append("<c r=\"").append(colName(i)).append("1\" t=\"inlineStr\"><is><t>");
            sheet.append(escape(cols.get(i)));
            sheet.append("</t></is></c>");
        }
        sheet.append("</row></sheetData></worksheet>");
        try {
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(raw, StandardCharsets.UTF_8);
            put(zip, "[Content_Types].xml", contentTypes());
            put(zip, "_rels/.rels", rels());
            put(zip, "xl/workbook.xml", workbook());
            put(zip, "xl/_rels/workbook.xml.rels", workbookRels());
            put(zip, "xl/worksheets/sheet1.xml", sheet.toString());
            zip.close();
            return raw.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException("无法生成导入模板");
        }
    }

    private static Map<String, byte[]> unzip(byte[] bytes) {
        Map<String, byte[]> parts = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                parts.put(entry.getName(), zip.readAllBytes());
            }
        } catch (Exception ex) {
            throw new BusinessException("无法解析 Excel 文件");
        }
        return parts;
    }

    private static byte[] firstSheet(Map<String, byte[]> parts) {
        for (Map.Entry<String, byte[]> item : parts.entrySet()) {
            String name = item.getKey().replace('\\', '/');
            if (name.startsWith("xl/worksheets/") && name.endsWith(".xml")) {
                return item.getValue();
            }
        }
        return null;
    }

    private static List<String> readShared(byte[] xml) {
        List<String> shared = new ArrayList<>();
        if (xml == null) {
            return shared;
        }
        Document doc = parse(xml);
        NodeList nodes = doc.getElementsByTagNameNS("*", "si");
        for (int i = 0; i < nodes.getLength(); i++) {
            shared.add(textOf(nodes.item(i)));
        }
        return shared;
    }

    private static List<List<String>> readSheet(byte[] xml, List<String> shared) {
        Document doc = parse(xml);
        NodeList rows = doc.getElementsByTagNameNS("*", "row");
        List<List<String>> table = new ArrayList<>();
        int width = 0;
        for (int i = 0; i < rows.getLength(); i++) {
            Element row = (Element) rows.item(i);
            NodeList cells = row.getElementsByTagNameNS("*", "c");
            List<Holder> holders = new ArrayList<>();
            int max = -1;
            for (int j = 0; j < cells.getLength(); j++) {
                Element cell = (Element) cells.item(j);
                int col = colIndex(cell.getAttribute("r"));
                holders.add(new Holder(col, cellText(cell, shared)));
                if (col > max) {
                    max = col;
                }
            }
            if (max + 1 > width) {
                width = max + 1;
            }
            List<String> line = new ArrayList<>();
            for (int k = 0; k <= max; k++) {
                line.add("");
            }
            for (Holder holder : holders) {
                if (holder.col >= 0 && holder.col < line.size()) {
                    line.set(holder.col, holder.text);
                }
            }
            table.add(line);
        }
        for (List<String> line : table) {
            while (line.size() < width) {
                line.add("");
            }
        }
        return table;
    }

    private static String cellText(Element cell, List<String> shared) {
        String type = cell.getAttribute("t");
        if ("inlineStr".equals(type)) {
            return textOf(cell);
        }
        NodeList values = cell.getElementsByTagNameNS("*", "v");
        if (values.getLength() == 0) {
            return "";
        }
        String raw = values.item(0).getTextContent();
        if (raw == null) {
            return "";
        }
        raw = raw.trim();
        if ("s".equals(type)) {
            try {
                int idx = Integer.parseInt(raw);
                if (idx >= 0 && idx < shared.size()) {
                    return shared.get(idx);
                }
            } catch (NumberFormatException ignored) {
                return raw;
            }
            return "";
        }
        return numberAsText(raw);
    }

    private static String numberAsText(String raw) {
        try {
            double num = Double.parseDouble(raw);
            if (Math.abs(num - Math.rint(num)) < 0.0000001) {
                return Long.toString((long) Math.rint(num));
            }
            return raw;
        } catch (NumberFormatException ex) {
            return raw;
        }
    }

    private static int colIndex(String ref) {
        if (ref == null || ref.isBlank()) {
            return 0;
        }
        int idx = 0;
        for (int i = 0; i < ref.length(); i++) {
            char ch = ref.charAt(i);
            if (ch < 'A' || ch > 'Z') {
                break;
            }
            idx = idx * 26 + (ch - 'A' + 1);
        }
        return Math.max(0, idx - 1);
    }

    private static String colName(int index) {
        int n = index + 1;
        StringBuilder out = new StringBuilder();
        while (n > 0) {
            int rem = (n - 1) % 26;
            out.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return out.toString();
    }

    private static Document parse(byte[] xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        } catch (Exception ex) {
            throw new BusinessException("无法解析 Excel 内容");
        }
    }

    private static String textOf(Node node) {
        StringBuilder out = new StringBuilder();
        collectText(node, out);
        return out.toString().trim();
    }

    private static void collectText(Node node, StringBuilder out) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            out.append(node.getNodeValue());
            return;
        }
        Node child = node.getFirstChild();
        while (child != null) {
            collectText(child, out);
            child = child.getNextSibling();
        }
    }

    private static void put(ZipOutputStream zip, String name, String xml) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(xml.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "</Types>";
    }

    private static String rels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"员工\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
    }

    private static String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "</Relationships>";
    }

    private static final class Holder {
        private final int col;
        private final String text;

        private Holder(int col, String text) {
            this.col = col;
            this.text = text;
        }
    }
}
