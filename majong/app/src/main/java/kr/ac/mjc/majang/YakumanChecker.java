package kr.ac.mjc.majang;

import java.util.*;

/**
 * YakumanChecker
 * - 입력된 tiles(List<String>)가 역만(야쿠만) 패턴에 해당하는지 판정하는 클래스
 * - MainActivity에서 사용하는 "1m", "9m", ..., "E", "S", "W", "N", "P", "F", "C" 그대로 비교
 * - 멘젠 한정 역과 후로 가능 역을 명확히 구분해서 체크
 */
public class YakumanChecker {

    // === 멘젠 한정 역만 (국사무쌍, 스안커, 구련보등, 순정구련보등) ===

    // 국사무쌍 (Kokushi Musou)
    public static boolean isKokushi(List<String> tiles, boolean isMenzen) {
        if (!isMenzen) return false;
        Set<String> required = Set.of(
                "1m", "9m", "1p", "9p", "1s", "9s",
                "E", "S", "W", "N", "P", "F", "C"
        );
        Map<String, Integer> count = new HashMap<>();
        for (String t : tiles) count.put(t, count.getOrDefault(t, 0) + 1);
        boolean pair = false;
        for (String key : required) {
            int n = count.getOrDefault(key, 0);
            if (n == 2) {
                if (pair) return false;
                pair = true;
            } else if (n == 0) {
                return false;
            }
        }
        return pair && tiles.size() == 14;
    }

    // 스안커 (Suanko) - 4개의 순수 쌍/퐁(멘젠 한정)
    public static boolean isSuanko(List<String> tiles, boolean isMenzen) {
        if (!isMenzen) return false;
        Map<String, Integer> count = new HashMap<>();
        for (String t : tiles) count.put(t, count.getOrDefault(t, 0) + 1);
        int triplets = 0;
        for (int v : count.values()) if (v >= 3) triplets++;
        return triplets >= 4;
    }

    // 구련보등 (Chuurenpoutou) - 멘젠 한정, 한 슈트에서만
    public static boolean isChuurenpoutou(List<String> tiles, boolean isMenzen) {
        if (!isMenzen) return false;
        if (tiles.size() != 14) return false;
        String suit = null;
        for (String t : tiles) {
            if (!(t.endsWith("m") || t.endsWith("p") || t.endsWith("s"))) return false;
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

    // 순정구련보등 (Junsei Chuurenpoutou)
    public static boolean isJunseiChuurenpoutou(List<String> tiles, String winTile, boolean isMenzen) {
        if (!isChuurenpoutou(tiles, isMenzen)) return false;
        if (winTile == null) return false;
        String suit = winTile.substring(1);
        for (String t : tiles) {
            if (!(t.endsWith("m") || t.endsWith("p") || t.endsWith("s"))) return false;
            if (!t.substring(1).equals(suit)) return false;
        }
        int num = Integer.parseInt(winTile.substring(0, 1));
        List<String> otherTiles = new ArrayList<>(tiles);
        otherTiles.remove(winTile);
        Map<Integer, Integer> count = new HashMap<>();
        for (String t : otherTiles) {
            count.put(Integer.parseInt(t.substring(0, 1)), count.getOrDefault(Integer.parseInt(t.substring(0, 1)), 0) + 1);
        }
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

    // === 후로 가능(멘젠 한정 아님) 역만 ===

    // 대삼원 (Daisangen)
    public static boolean isDaisangen(List<String> tiles) {
        return Collections.frequency(tiles, "P") >= 3
                && Collections.frequency(tiles, "F") >= 3
                && Collections.frequency(tiles, "C") >= 3;
    }

    // 자일색 (Tsuuiisou)
    public static boolean isTsuuiisou(List<String> tiles) {
        Set<String> honors = Set.of("E", "S", "W", "N", "P", "F", "C");
        for (String t : tiles) if (!honors.contains(t)) return false;
        return tiles.size() == 14;
    }

    // 녹일색 (Ryuuiisou)
    public static boolean isRyuuiisou(List<String> tiles) {
        Set<String> greens = Set.of("2s", "3s", "4s", "6s", "8s", "F");
        for (String t : tiles) if (!greens.contains(t)) return false;
        return tiles.size() == 14;
    }

    // 청노두 (Chinroutou)
    public static boolean isChinroutou(List<String> tiles) {
        Set<String> valid = Set.of("1m", "9m", "1p", "9p", "1s", "9s");
        for (String t : tiles) {
            if (!valid.contains(t)) return false;
        }
        return tiles.size() == 14;
    }

    // 소사희 (Shousuushi)
    public static boolean isShousuushi(List<String> tiles) {
        int pairs = 0, triplets = 0;
        for (String wind : List.of("E", "S", "W", "N")) {
            int cnt = Collections.frequency(tiles, wind);
            if (cnt == 2) pairs++;
            if (cnt >= 3) triplets++;
        }
        return pairs == 1 && triplets == 3;
    }

    // 대사희 (Daisuushi)
    public static boolean isDaisuushi(List<String> tiles) {
        for (String wind : List.of("E", "S", "W", "N")) {
            if (Collections.frequency(tiles, wind) < 3) return false;
        }
        return tiles.size() == 14;
    }

    // === 전체 역만 리스트 반환 ===
    public static List<String> getYakumanList(List<String> tiles, String winTile, boolean isMenzen) {
        List<String> yakuman = new ArrayList<>();
        // 멘젠 한정
        if (isKokushi(tiles, isMenzen)) yakuman.add("국사무쌍");
        if (isSuanko(tiles, isMenzen)) yakuman.add("스안커");
        if (isJunseiChuurenpoutou(tiles, winTile, isMenzen)) {
            yakuman.add("순정구련보등");
        } else if (isChuurenpoutou(tiles, isMenzen)) {
            yakuman.add("구련보등");
        }

        // 후로도 OK
        if (isDaisangen(tiles)) yakuman.add("대삼원");
        if (isTsuuiisou(tiles)) yakuman.add("자일색");
        if (isRyuuiisou(tiles)) yakuman.add("녹일색");
        if (isChinroutou(tiles)) yakuman.add("청노두");
        if (isShousuushi(tiles)) yakuman.add("소사희");
        if (isDaisuushi(tiles)) yakuman.add("대사희");
        return yakuman;
    }
}
