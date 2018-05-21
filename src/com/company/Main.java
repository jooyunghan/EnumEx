package com.company;

import com.x.Color;

import java.util.Arrays;

public class Main {

  public static void main(String[] args) {
    System.out.println(Arrays.toString(Color.values()));
    System.out.println(Arrays.toString(X.values()));
  }
}

class X {
  public static X a;
  public static X b;
  public static X[] xs;

  public static X[] values() {
    return xs;
  }

  static {
    xs = new X[10000];
    XInit.init();
  }

}

class XInit {
  static void init() {
    X.a = new X();
    X.b = new X();
    X.xs[0] = X.a;
    X.xs[1] = X.b;
  }
}
