package kr.ac.mjc.majang;

import java.util.*;

public class YakumanChecker {

    // 국사무쌍 (Kokushi Musou) - 13 야오츄 + 1쌍
    public static boolean isKokushi(List<String> tiles) {
        Set<String> required = Set.of(
                "1m", "9m", "1t", "9t", "1s", "9s",
                "z1", "z2", "z3", "z4", "z5", "z6", "z7"
        );
        Map<String, Integer> count = new HashMap<>();
        for (String t : tiles) count.put(t, count.getOrDefault(t, 0) + 1);
        boolean pair = false;
        for (String key : required) {
            int n = count.getOrDefault(key, 0);
            if (n == 2) {
                if (pair) return false; // 중복 쌍 불가
                pair = true;
            } else if (n == 0) {
                return false;
            }
        }
        return pair && tiles.size() == 14;
    }

    // 청노두 (Chinroutou) - 모두 1,9수패로만 구성
    public static boolean isChinroutou(List<String> tiles) {
        Set<String> valid = Set.of("1m", "9m", "1t", "9t", "1s", "9s");
        for (String t : tiles) {
            if (!valid.contains(t)) return false;
        }
        return tiles.size() == 14;
    }

    // 대삼원 (Daisangen) - z5(백), z6(발), z7(중) 각각 3장 이상
    public static boolean isDaisangen(List<String> tiles) {
        return Collections.frequency(tiles, "z5") >= 3
                && Collections.frequency(tiles, "z6") >= 3
                && Collections.frequency(tiles, "z7") >= 3;
    }

    // 스안커 (Suanko) - 4개의 트리플렛(셋트)
    public static boolean isSuanko(List<String> tiles) {
        Map<String, Integer> count = new HashMap<>();
        for (String t : tiles) count.put(t, count.getOrDefault(t, 0) + 1);
        int triplets = 0;
        for (int v : count.values()) if (v >= 3) triplets++;
        return triplets >= 4;
    }

    // 스깡쯔 (Sukantsu) - 4개의 깡(같은패 4장) 스깡즈는 로직 상 구현하기 어려움. 따라서 어쩔수 없이 빼게됨.
    //public static boolean isSukantsu(List<String> tiles) {
        //Map<String, Integer> count = new HashMap<>();
        //for (String t : tiles) count.put(t, count.getOrDefault(t, 0) + 1);
        //int quads = 0;
        //for (int v : count.values()) if (v == 4) quads++;
        //return quads == 4;
   // }

    // 자일색 (Tsuuiisou) - 모두 자패(z1~z7)만 구성
    public static boolean isTsuuiisou(List<String> tiles) {
        Set<String> honors = Set.of("z1", "z2", "z3", "z4", "z5", "z6", "z7");
        for (String t : tiles) if (!honors.contains(t)) return false;
        return tiles.size() == 14;
    }

    // 녹일색 (Ryuuiisou) - 2s,3s,4s,6s,8s,z6(발)만 사용
    public static boolean isRyuuiisou(List<String> tiles) {
        Set<String> greens = Set.of("2s", "3s", "4s", "6s", "8s", "z6");
        for (String t : tiles) if (!greens.contains(t)) return false;
        return tiles.size() == 14;
    }

    // 소사희 (Shousuushi) - z1~z4(동남서북) 중 3종류 트리플렛+1쌍
    public static boolean isShousuushi(List<String> tiles) {
        int pairs = 0, triplets = 0;
        for (String wind : List.of("z1", "z2", "z3", "z4")) {
            int cnt = Collections.frequency(tiles, wind);
            if (cnt == 2) pairs++;
            if (cnt >= 3) triplets++;
        }
        return pairs == 1 && triplets == 3;
    }

    // 대사희 (Daisuushi) - z1~z4 각각 3장 이상
    public static boolean isDaisuushi(List<String> tiles) {
        for (String wind : List.of("z1", "z2", "z3", "z4")) {
            if (Collections.frequency(tiles, wind) < 3) return false;
        }
        return tiles.size() == 14;
    }

    // 일반 구련보등 (Chuurenpoutou, 아가리 관계 없음)
    public static boolean isChuurenpoutou(List<String> tiles) {
        if (tiles.size() != 14) return false;
        String suit = null;
        for (String t : tiles) {
            if (!(t.endsWith("m") || t.endsWith("t") || t.endsWith("s"))) return false;
            String thisSuit = t.substring(1);
            if (suit == null) suit = thisSuit;
            if (!thisSuit.equals(suit)) return false;
        }
        Map<Integer, Integer> count = new HashMap<>();
        for (String t : tiles) {
            int num = Integer.parseInt(t.substring(0, 1));
            count.put(num, count.getOrDefault(num, 0) + 1);
        }
        if (count.getOrDefault(1, 0) < 3) return false;
        if (count.getOrDefault(9, 0) < 3) return false;
        for (int i = 2; i <= 8; i++) {
            if (count.getOrDefault(i, 0) < 1) return false;
        }
        return true;
    }

    // 순정 구련보등 (Junsei Chuurenpoutou, 아가리 타일로 완성)
    // winTile: 아가리 타일(마지막에 들어온 패), 없으면 null
    public static boolean isJunseiChuurenpoutou(List<String> tiles, String winTile) {
        if (!isChuurenpoutou(tiles)) return false;
        if (winTile == null) return false; // 아가리 미지정
        // winTile이 반드시 해당 슈트의 1~9여야 함
        String suit = winTile.substring(1);
        for (String t : tiles) {
            if (!(t.endsWith("m") || t.endsWith("t") || t.endsWith("s"))) return false;
            if (!t.substring(1).equals(suit)) return false;
        }
        int num = Integer.parseInt(winTile.substring(0, 1));
        // winTile을 제외한 13패를 카운트
        List<String> otherTiles = new ArrayList<>(tiles);
        otherTiles.remove(winTile); // 한 번만 제거 (중복 있을 수 있음)
        Map<Integer, Integer> count = new HashMap<>();
        for (String t : otherTiles) {
            count.put(Integer.parseInt(t.substring(0, 1)), count.getOrDefault(Integer.parseInt(t.substring(0, 1)), 0) + 1);
        }
        // 1,9: 2장, winTile만 1장 더 있음. 2~8: 1장씩
        if (num == 1 || num == 9) {
            if (count.getOrDefault(num, 0) != 2) return false;
        } else {
            if (count.getOrDefault(num, 0) != 1) return false;
        }
        for (int i = 1; i <= 9; i++) {
            if (i == num) continue;
            if (i == 1 || i == 9) {
                if (count.getOrDefault(i, 0) != 3) return false;
            } else {
                if (count.getOrDefault(i, 0) != 1) return false;
            }
        }
        return true;
    }

    // 역만 리스트 반환
    public static List<String> getYakumanList(List<String> tiles, String winTile) {
        List<String> yakuman = new ArrayList<>();
        if (isKokushi(tiles)) yakuman.add("국사무쌍");
        if (isChinroutou(tiles)) yakuman.add("청노두");
        if (isDaisangen(tiles)) yakuman.add("대삼원");
        if (isSuanko(tiles)) yakuman.add("스안커");
        //if (isSukantsu(tiles)) yakuman.add("스깡쯔");
        if (isTsuuiisou(tiles)) yakuman.add("자일색");
        if (isRyuuiisou(tiles)) yakuman.add("녹일색");
        if (isShousuushi(tiles)) yakuman.add("소사희");
        if (isDaisuushi(tiles)) yakuman.add("대사희");
        if (isJunseiChuurenpoutou(tiles, winTile)) {
            yakuman.add("순정구련보등");
        } else if (isChuurenpoutou(tiles)) {
            yakuman.add("구련보등");
        }
        return yakuman;
    }
}
