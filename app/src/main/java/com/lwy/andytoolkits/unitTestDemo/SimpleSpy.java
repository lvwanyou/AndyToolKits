package com.lwy.andytoolkits.unitTestDemo;

//  间谍mock技术，针对特定类， 仅mock某个方法
public class SimpleSpy {

    /**
     * 魔法盒子, 薛定谔的猫在里边，
     *
     * @return "died" or "alive"
     */
    String magicBox() {
        throw new RuntimeException("遇事不决，量子力学");
    }


    public boolean isSchrodingerRight() {
        // 测试3000次， 看看结果如何
        int diedNum = 0;
        for (int i = 0; i < 3000; i++) {
            String result = magicBox();
            if ("died".equals(result)) {
                diedNum += 1;
            }
        }
        return diedNum != 0 && diedNum != 3000;
    }
}
