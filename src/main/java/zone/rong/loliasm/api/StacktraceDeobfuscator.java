package zone.rong.loliasm.api;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.*;
import java.util.*;

public final class StacktraceDeobfuscator {

    private static Map<String, String> srgMcpMethodMap = null;

    /**
     * If the file does not exits, downloads latest method mappings and saves them to it.
     * Initializes a HashMap between obfuscated and deobfuscated names from that file.
     */
    public static void init(File mappings) {
        try (InputStream inputStream = new FileInputStream(mappings)) {
            init(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read " + mappings.getAbsolutePath() + " as srg<->mcp mappings.", e);
        }
    }

    public static void init(InputStream is) {
        if (srgMcpMethodMap != null) {
            return;
        }
        // Read the mapping
        Map<String, String> srgMcpMethodMap = new Object2ObjectOpenHashMap<>();
        try (Scanner scanner = new Scanner(is)) {
            scanner.nextLine(); // Skip CSV header
            while (scanner.hasNext()) {
                String mappingLine = scanner.nextLine();
                int commaIndex = mappingLine.indexOf(',');
                String srgName = mappingLine.substring(0, commaIndex);
                String mcpName = mappingLine.substring(commaIndex + 1, commaIndex + 1 + mappingLine.substring(commaIndex + 1).indexOf(','));
                srgMcpMethodMap.put(srgName, mcpName);
            }
        }
        // Set the map only if it's successful, to make sure that it's complete
        StacktraceDeobfuscator.srgMcpMethodMap = srgMcpMethodMap;
    }

    public static void deobfuscateThrowable(Throwable t) {
        Deque<Throwable> queue = new ArrayDeque<>();
        queue.add(t);
        while (!queue.isEmpty()) {
            t = queue.remove();
            t.setStackTrace(deobfuscateStacktrace(t.getStackTrace()));
            if (t.getCause() != null) {
                queue.add(t.getCause());
            }
            Collections.addAll(queue, t.getSuppressed());
        }
    }

    public static StackTraceElement[] deobfuscateStacktrace(StackTraceElement[] stackTrace) {
        int index = 0;
        for (StackTraceElement el : stackTrace) {
            stackTrace[index++] = new StackTraceElement(el.getClassName(), deobfuscateMethodName(el.getMethodName()), el.getFileName(), el.getLineNumber());
        }
        return stackTrace;
    }

    public static String deobfuscateMethodName(String srgName) {
        if (srgMcpMethodMap == null) {
            return srgName;
        }
        String mcpName = srgMcpMethodMap.get(srgName);
        return mcpName != null ? mcpName : srgName;
    }

    public static void main(String[] args) {
        init(new File("methods.csv"));
        for (Map.Entry<String, String> entry : srgMcpMethodMap.entrySet()) {
            System.out.println(entry.getKey() + " <=> " + entry.getValue());
        }
    }

}
