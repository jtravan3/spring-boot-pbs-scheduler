package com.jtravan.pbs.model;

public enum Resource {
    A(0, false),
    B(1, false),
    C(2, false),
    D(3, false),
    E(4, false),
    F(5, false),
    G(6, false),
    H(7, false),
    I(8, false),
    J(9, false),
    K(10,false),
    L(11,false),
    M(12,false),
    N(13,false),
    O(14,false),
    P(15,false),
    Q(16,false),
    R(17,false),
    S(18,false),
    T(19,false),
    U(20,false),
    V(21,false),
    W(22,false),
    X(23,false),
    Y(24,false),
    Z(25,false),
    AA(26,false),
    BB(27,false),
    CC(28,false),
    DD(29,false),
    EE(30,false),
    FF(31,false),
    GG(32,false),
    HH(33,false),
    II(34,false),
    JJ(35,false),
    KK(36,false),
    LL(37,false),
    MM(38,false),
    NN(39,false),
    OO(40,false),
    PP(41,false),
    QQ(42,false),
    RR(43,false),
    SS(44,false),
    TT(45,false),
    UU(46,false),
    VV(47,false),
    WW(48,false),
    XX(49,false),
    YY(50,false),
    ZZ(51,false),
    AAA(52,false),
    BBB(53,false),
    CCC(54,false),
    DDD(55,false),
    EEE(56,false),
    FFF(57,false),
    GGG(58,false),
    HHH(59,false),
    III(60,false),
    JJJ(61,false),
    KKK(62,false),
    LLL(63,false),
    MMM(64,false),
    NNN(65,false),
    OOO(66,false),
    PPP(67,false),
    QQQ(68,false),
    RRR(69,false),
    SSS(70,false),
    TTT(71,false),
    UUU(72,false),
    VVV(73,false),
    WWW(74,false),
    XXX(75,false),
    YYY(76,false),
    ZZZ(77,false);

    private final int resourceNum;
    private boolean isLocked;

    Resource(int resourceNum, boolean isLocked) {

        this.resourceNum = resourceNum;
        this.isLocked = isLocked;
    }

    public final int getResourceNum() {
        return resourceNum;
    }

    public final synchronized boolean isLocked() {
        return this.isLocked;
    }

    public final synchronized void lock() {
        this.isLocked = true;
    }

    public final synchronized void unlock() {
        this.isLocked = false;
    }
}
