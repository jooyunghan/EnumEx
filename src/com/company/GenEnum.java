package com.company;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static org.objectweb.asm.Opcodes.*;

class EnumClass {
  private final String pkg;
  private final String className;
  private final String[] elements;

  public EnumClass(String pkg, String className, String[] elements) {
    this.pkg = pkg;
    this.className = className;
    this.elements = elements;
  }

  public void writeClassFileTo(String classpath) throws IOException {
    String selfName = pkg.replace('.', '/') + "/" + className;
    String selfType = "L" + selfName + ";";
    String selfArrType = "[" + selfType;
    ClassWriter cw = new ClassWriter(0);
    cw.visit(V1_5, ACC_PUBLIC + ACC_ENUM + ACC_FINAL + ACC_SUPER, selfName, null, "java/lang/Enum", null);
    for (String e : elements) {
      cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, e, selfType, null, null).visitEnd();
    }

    {
      cw.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL + ACC_SYNTHETIC, "$VALUES", selfArrType, null, null).visitEnd();
    }

    {
      final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "valueOf", "(Ljava/lang/String;)" + selfType, null, null);
      mv.visitLdcInsn(Type.getType(selfType));
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
      mv.visitTypeInsn(CHECKCAST, selfName);
      mv.visitInsn(ARETURN);
      mv.visitMaxs(2, 1);
      mv.visitEnd();
    }

    {
      final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "values", "()" + selfArrType, null, null);
      mv.visitFieldInsn(GETSTATIC, selfName, "$VALUES", selfArrType);
      mv.visitMethodInsn(INVOKEVIRTUAL, selfArrType, "clone", "()Ljava/lang/Object;", false);
      mv.visitTypeInsn(CHECKCAST, selfArrType);
      mv.visitInsn(ARETURN);
      mv.visitMaxs(1, 0);
      mv.visitEnd();
    }

    {
      final MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
      for (int i = 0; i < elements.length; i++) {
        String e = elements[i];
        mv.visitTypeInsn(NEW, selfName);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(e);
        visitInt(mv, i);
        mv.visitMethodInsn(INVOKESPECIAL, selfName, "<init>", "(Ljava/lang/String;I)V", false);
        mv.visitFieldInsn(PUTSTATIC, selfName, e, selfType);
      }

      visitInt(mv, elements.length);
      mv.visitTypeInsn(ANEWARRAY, selfName);
      for (int i = 0; i < elements.length; i++) {
        String e = elements[i];
        mv.visitInsn(DUP);
        visitInt(mv, i);
        mv.visitFieldInsn(GETSTATIC, selfName, e, selfType);
        mv.visitInsn(AASTORE);
      }

      mv.visitFieldInsn(PUTSTATIC, selfName, "$VALUES", selfArrType);
      mv.visitInsn(RETURN);
      mv.visitMaxs(4, 0);
      mv.visitEnd();
    }

    {
      final MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V", null, null);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitVarInsn(ILOAD, 2);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V", false);
      mv.visitInsn(RETURN);
      mv.visitMaxs(3, 3);
      mv.visitEnd();
    }
    cw.visitEnd();
    Files.createDirectories(Paths.get(classpath, pkg.replace('.', File.separatorChar)));
    Files.write(Paths.get(classpath, pkg.replace('.', File.separatorChar), className + ".class"), cw.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  private void visitInt(MethodVisitor mv, int i) {
    switch (i) {
      case 0:
        mv.visitInsn(ICONST_0);
        break;
      case 1:
        mv.visitInsn(ICONST_1);
        break;
      case 2:
        mv.visitInsn(ICONST_2);
        break;
      case 3:
        mv.visitInsn(ICONST_3);
        break;
      case 4:
        mv.visitInsn(ICONST_4);
        break;
      case 5:
        mv.visitInsn(ICONST_5);
        break;
      default:
        mv.visitIntInsn(BIPUSH, i);
        break;
    }
  }
}

public class GenEnum {
  public static void main(String[] args) throws IOException {
    ArrayList<String> es = new ArrayList<>();
    for (int i=0; i<3; i++) {
      es.add("VAR" + i);
    }
    new EnumClass("com.x", "Color", es.toArray(new String[0])).writeClassFileTo("classes");
  }

}
