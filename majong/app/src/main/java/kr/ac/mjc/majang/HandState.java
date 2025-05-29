package kr.ac.mjc.majang;

import java.io.Serializable;
import java.util.*;

public class HandState implements Serializable {
    public List<String> tiles = new ArrayList<>();
    public List<String> yakuList = new ArrayList<>();
    public int dora = 0;
    public int fu = 20;
    public boolean isDealer = false;
    public boolean isTsumo = false;
    public boolean isFuro = false;     // 오픈 멘츠(후로) 여부
    public boolean isMenzen = true;    // 멘젠(오픈 안됨) 여부

    // 생성자 등 필요시 추가
}
