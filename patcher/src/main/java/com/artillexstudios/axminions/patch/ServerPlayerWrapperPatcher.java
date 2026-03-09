package com.artillexstudios.axminions.patch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public final class ServerPlayerWrapperPatcher {
    private static final String TARGET_ENTRY = "com/artillexstudios/axminions/libs/axapi/nms/v1_21_R1/wrapper/ServerPlayerWrapper.class";
    private static final String ATTRIBUTE_MAP_BASE = "net.minecraft.world.entity.ai.attributes.AttributeMapBase";
    private static final String ATTRIBUTE_FIELD_NAME = "e";
    private static final String ATTRIBUTE_FIELD_FALLBACK = "supplier";
    private static final String COMPONENT_SERIALIZER_ENTRY = "com/artillexstudios/axminions/libs/axapi/utils/ComponentSerializer.class";
    private static final Map<String, String> COMPONENT_SER_STRING_REPLACEMENTS = Map.of(
            "net.minecraft.network.chat.Component$ChatSerializer", "net.minecraft.network.chat.Component$Serializer"
    );

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Path to jar file is required");
        }
        File jarFile = new File(args[0]);
        if (!jarFile.isFile()) {
            throw new IllegalArgumentException("Jar file does not exist: " + jarFile.getPath());
        }
        patchJar(jarFile);
    }

    private static void patchJar(File jarFile) throws Exception {
        File tempJar = Files.createTempFile(jarFile.getParentFile() != null ? jarFile.getParentFile().toPath() : new File(".").toPath(), "patched", ".jar").toFile();
        try (JarFile jar = new JarFile(jarFile); JarOutputStream output = new JarOutputStream(Files.newOutputStream(tempJar.toPath()))) {
            jar.stream().forEach(entry -> {
                try {
                    JarEntry newEntry = new JarEntry(entry.getName());
                    newEntry.setTime(entry.getTime());
                    output.putNextEntry(newEntry);
                    if (entry.isDirectory()) {
                        output.closeEntry();
                        return;
                    }
                    byte[] bytes = jar.getInputStream(entry).readAllBytes();
                    if (entry.getName().equals(TARGET_ENTRY)) {
                        output.write(patchClass(bytes));
                    } else if (entry.getName().equals(COMPONENT_SERIALIZER_ENTRY)) {
                        output.write(patchClassStrings(bytes, COMPONENT_SER_STRING_REPLACEMENTS));
                    } else {
                        output.write(bytes);
                    }
                    output.closeEntry();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to patch jar", e);
                }
            });
        }
        Files.move(tempJar.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private static byte[] patchClass(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("<clinit>".equals(name) && "()V".equals(descriptor)) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        private boolean sawAttributeMapClass = false;

                        @Override
                        public void visitLdcInsn(Object value) {
                            if (value instanceof Type && ATTRIBUTE_MAP_BASE.equals(((Type) value).getClassName())) {
                                sawAttributeMapClass = true;
                                super.visitLdcInsn(value);
                                return;
                            }
                            if (sawAttributeMapClass && ATTRIBUTE_FIELD_NAME.equals(value)) {
                                sawAttributeMapClass = false;
                                super.visitLdcInsn(ATTRIBUTE_FIELD_FALLBACK);
                                return;
                            }
                            sawAttributeMapClass = false;
                            super.visitLdcInsn(value);
                        }
                    };
                }
                return mv;
            }
        }, ClassReader.SKIP_FRAMES);
        return writer.toByteArray();
    }

    private static byte[] patchClassStrings(byte[] bytes, Map<String, String> replacements) {
        byte[] patched = bytes;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            byte[] target = entry.getKey().getBytes(StandardCharsets.UTF_8);
            byte[] replacement = entry.getValue().getBytes(StandardCharsets.UTF_8);
            patched = replaceAll(patched, target, replacement);
        }
        return patched;
    }

    private static byte[] replaceAll(byte[] data, byte[] target, byte[] replacement) {
        if (target.length == 0) {
            return data;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int i = 0;
        while (i < data.length) {
            int idx = indexOf(data, target, i);
            if (idx < 0) {
                buffer.write(data, i, data.length - i);
                break;
            }
            buffer.write(data, i, idx - i);
            buffer.write(replacement, 0, replacement.length);
            i = idx + target.length;
        }
        return buffer.toByteArray();
    }

    private static int indexOf(byte[] data, byte[] target, int start) {
        outer:
        for (int i = start; i <= data.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (data[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
