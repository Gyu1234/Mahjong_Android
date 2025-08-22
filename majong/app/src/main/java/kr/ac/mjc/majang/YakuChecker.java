package kr.ac.mjc.majang;

import java.util.*;

public class YakuChecker {

    public static class YakuResult {
        public String name;
        public int han;
        public YakuResult(String name, int han) {
            this.name = name;
            this.han = han;
        }
    }

    /**
     * 판수 큰 쪽만 남기고, 치또이 25부 보장, 오야 고려
     */
    public static List<YakuResult> getYakuList(HandState hand) {
        List<YakuResult> yaku = new ArrayList<>();

        // [1] 치또이/량페코/이페코 (배타, 큰 쪽 우선)
        boolean chitoitsu = isChitoitsu(hand);
        boolean ryanpeko = isRyanpeko(hand);
        boolean iipeikou = isIipeikou(hand);

        if (ryanpeko) {
            yaku.add(new YakuResult("량페코", 3));
        } else if (chitoitsu) {
            yaku.add(new YakuResult("치또이", 2));
        } else if (iipeikou) {
            yaku.add(new YakuResult("이페코", 1));
        }

        // [2] 혼노두/준찬타/찬타(배타, 큰 쪽 우선)
        int honroutouHan = isHonroutou(hand) ? 2 : 0;
        int junchanHan = isJunchanMeldSearch(hand.tiles) ? (hand.isMenzen ? 2 : 1) : 0;
        int chantaHan = isChantaMeldSearch(hand.tiles) ? (hand.isMenzen ? 2 : 1) : 0;

        if (honroutouHan >= junchanHan && honroutouHan >= chantaHan && honroutouHan > 0) {
            yaku.add(new YakuResult("혼노두", honroutouHan));
        } else if (junchanHan >= chantaHan && junchanHan > 0) {
            yaku.add(new YakuResult("준찬타", junchanHan));
        } else if (chantaHan > 0) {
            yaku.add(new YakuResult("찬타", chantaHan));
        }

        // [3] 멘젠 한정 (일배고 제외 - 위에서 이미 처리됨)
        if (hand.isMenzen) {
            if (isRiichi(hand)) yaku.add(new YakuResult("리치", 1));
            if (isPinfu(hand)) yaku.add(new YakuResult("핑후", 1));
            if (isTsumo(hand)) yaku.add(new YakuResult("쯔모", 1));
            if (isIppatsu(hand)) yaku.add(new YakuResult("일발", 1));
            if (isDoubleRiichi(hand)) yaku.add(new YakuResult("더블리치", 2));
        }

        // [4] 기타 중복 허용 역
        if (isSanshokuDoujun(hand)) yaku.add(new YakuResult("삼색동순", hand.isMenzen ? 2 : 1));
        if (isIkkitsuukan(hand)) yaku.add(new YakuResult("일기통관", hand.isMenzen ? 2 : 1));
        if (isChinitsu(hand)) yaku.add(new YakuResult("청일색", hand.isMenzen ? 6 : 5));
        if (isHonitsu(hand)) yaku.add(new YakuResult("혼일색", hand.isMenzen ? 3 : 2));
        if (isYakuhai(hand)) yaku.add(new YakuResult("역패", 1));
        if (isSanshokuDokko(hand)) yaku.add(new YakuResult("삼색동각", 2));
        if (isShousangen(hand)) yaku.add(new YakuResult("소삼원", 2));
        if (isTanyao(hand)) yaku.add(new YakuResult("탕야오", 1));
        if (isToitoi(hand)) yaku.add(new YakuResult("또이또이", 2));
        if (isSanankou(hand)) yaku.add(new YakuResult("삼암각", 2));
        if (isSankantsu(hand)) yaku.add(new YakuResult("산깡즈", 2));
        if (isHonroto(hand)) yaku.add(new YakuResult("혼노두", 2));

        return yaku;
    }

    // ==================== [기존 함수들 유지] ====================

    public static boolean isRiichi(HandState hand) {
        return hand.isMenzen && hand.yakuList != null && hand.yakuList.contains("리치");
    }

    public static boolean isTsumo(HandState hand) {
        return hand.isMenzen && hand.isTsumo;
    }

    public static boolean isIppatsu(HandState hand) {
        return hand.yakuList != null && hand.yakuList.contains("일발");
    }

    public static boolean isDoubleRiichi(HandState hand) {
        return hand.yakuList != null && hand.yakuList.contains("더블리치");
    }

    public static boolean isPinfu(HandState hand) {
        if (!hand.isMenzen) return false;
        List<String> tiles = hand.tiles;
        if (tiles == null || tiles.size() != 14) return false;

        Map<String, Integer> counts = new HashMap<>();
        for (String t : tiles) counts.put(t, counts.getOrDefault(t, 0) + 1);

        List<String> pairs = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() >= 2) pairs.add(e.getKey());
        }

        boolean validPinfu = false;
        for (String pair : pairs) {
            Map<String, Integer> tempCounts = new HashMap<>(counts);
            tempCounts.put(pair, tempCounts.get(pair) - 2);

            int meldCount = 0;
            while (meldCount < 4) {
                boolean found = false;
                for (char suit : new char[]{'m', 'p', 's'}) {
                    for (int n = 1; n <= 7; n++) {
                        String a = n + String.valueOf(suit);
                        String b = (n+1) + String.valueOf(suit);
                        String c = (n+2) + String.valueOf(suit);
                        if (tempCounts.getOrDefault(a,0) >= 1 &&
                                tempCounts.getOrDefault(b,0) >= 1 &&
                                tempCounts.getOrDefault(c,0) >= 1) {
                            tempCounts.put(a, tempCounts.get(a)-1);
                            tempCounts.put(b, tempCounts.get(b)-1);
                            tempCounts.put(c, tempCounts.get(c)-1);
                            found = true;
                            meldCount++;
                            break;
                        }
                    }
                    if (found) break;
                }
                if (!found) break;
            }
            if (meldCount == 4) {
                if (pair.startsWith("z")) continue;
                validPinfu = true;
                break;
            }
        }
        return validPinfu;
    }

    public static boolean isChitoitsu(HandState hand) {
        if (!hand.isMenzen) return false;
        Map<String, Integer> map = new HashMap<>();
        for (String t : hand.tiles) map.put(t, map.getOrDefault(t, 0) + 1);
        if (map.size() != 7) return false;
        for (int v : map.values()) {
            if (v != 2) return false;
        }
        return true;
    }

    /**
     * 이페코 - 같은 순자 2개가 정확히 하나만 있어야 함 (멘젠 한정)
     * 량페코와 배타적 관계
     */
    public static boolean isIipeikou(HandState hand) {
        if (!hand.isMenzen) return false;

        // 량페코가 성립하면 이페코는 무효
        if (isRyanpeko(hand)) return false;

        Map<String, Integer> counts = new HashMap<>();
        for (String t : hand.tiles) {
            counts.put(t, counts.getOrDefault(t, 0) + 1);
        }

        // 각 가능한 머리패로 시도
        for (String pair : counts.keySet()) {
            if (counts.get(pair) >= 2) {
                Map<String, Integer> pool = new HashMap<>(counts);
                pool.put(pair, pool.get(pair) - 2);

                List<String> foundMelds = new ArrayList<>();
                if (findAllMelds(pool, foundMelds)) {
                    // 순자만 확인 (각자는 제외)
                    Map<String, Integer> shuntsuCount = new HashMap<>();
                    for (String meld : foundMelds) {
                        if (isShuntsu(meld)) {
                            shuntsuCount.put(meld, shuntsuCount.getOrDefault(meld, 0) + 1);
                        }
                    }

                    // 정확히 하나의 순자가 2개 있어야 이페코
                    int duplicateShuntsu = 0;
                    for (int count : shuntsuCount.values()) {
                        if (count == 2) duplicateShuntsu++;
                    }

                    if (duplicateShuntsu == 1) return true;
                }
            }
        }
        return false;
    }

    /**
     * 량페코 - 같은 순자 2개가 2쌍 있어야 함 (멘젠 한정)
     */
    public static boolean isRyanpeko(HandState hand) {
        if (!hand.isMenzen) return false;

        Map<String, Integer> counts = new HashMap<>();
        for (String t : hand.tiles) {
            counts.put(t, counts.getOrDefault(t, 0) + 1);
        }

        // 각 가능한 머리패로 시도
        for (String pair : counts.keySet()) {
            if (counts.get(pair) >= 2) {
                Map<String, Integer> pool = new HashMap<>(counts);
                pool.put(pair, pool.get(pair) - 2);

                List<String> foundMelds = new ArrayList<>();
                if (findAllMelds(pool, foundMelds)) {
                    // 순자만 확인 (각자는 제외)
                    Map<String, Integer> shuntsuCount = new HashMap<>();
                    for (String meld : foundMelds) {
                        if (isShuntsu(meld)) {
                            shuntsuCount.put(meld, shuntsuCount.getOrDefault(meld, 0) + 1);
                        }
                    }

                    // 2개 이상의 순자가 각각 2개씩 있어야 량페코
                    int duplicateShuntsu = 0;
                    for (int count : shuntsuCount.values()) {
                        if (count == 2) duplicateShuntsu++;
                    }

                    if (duplicateShuntsu >= 2) return true;
                }
            }
        }
        return false;
    }

    /**
     * 모든 멘츠를 찾는 헬퍼 함수
     */
    private static boolean findAllMelds(Map<String, Integer> pool, List<String> melds) {
        // 남은 패가 없으면 성공
        boolean hasRemaining = false;
        for (int count : pool.values()) {
            if (count > 0) {
                hasRemaining = true;
                break;
            }
        }
        if (!hasRemaining) return true;

        // 각자(트리플릿) 먼저 시도
        for (String tile : pool.keySet()) {
            if (pool.get(tile) >= 3) {
                Map<String, Integer> newPool = new HashMap<>(pool);
                newPool.put(tile, newPool.get(tile) - 3);
                List<String> newMelds = new ArrayList<>(melds);
                newMelds.add(tile + tile + tile); // 각자 표시
                if (findAllMelds(newPool, newMelds)) {
                    melds.clear();
                    melds.addAll(newMelds);
                    return true;
                }
            }
        }

        // 순자 시도
        for (char suit : new char[]{'m', 'p', 's'}) {
            for (int i = 1; i <= 7; i++) {
                String a = i + String.valueOf(suit);
                String b = (i + 1) + String.valueOf(suit);
                String c = (i + 2) + String.valueOf(suit);

                if (pool.getOrDefault(a, 0) >= 1 &&
                        pool.getOrDefault(b, 0) >= 1 &&
                        pool.getOrDefault(c, 0) >= 1) {

                    Map<String, Integer> newPool = new HashMap<>(pool);
                    newPool.put(a, newPool.get(a) - 1);
                    newPool.put(b, newPool.get(b) - 1);
                    newPool.put(c, newPool.get(c) - 1);
                    List<String> newMelds = new ArrayList<>(melds);
                    newMelds.add(a + b + c); // 순자 표시
                    if (findAllMelds(newPool, newMelds)) {
                        melds.clear();
                        melds.addAll(newMelds);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 순자인지 확인하는 헬퍼 함수
     */
    private static boolean isShuntsu(String meld) {
        if (meld.length() != 6) return false; // 3개 패 = 6글자

        String first = meld.substring(0, 2);
        String second = meld.substring(2, 4);
        String third = meld.substring(4, 6);

        // 같은 수트인지 확인
        if (!first.substring(1).equals(second.substring(1)) ||
                !second.substring(1).equals(third.substring(1))) {
            return false;
        }

        // 연속된 숫자인지 확인
        try {
            int num1 = Integer.parseInt(first.substring(0, 1));
            int num2 = Integer.parseInt(second.substring(0, 1));
            int num3 = Integer.parseInt(third.substring(0, 1));
            return (num2 == num1 + 1) && (num3 == num2 + 1);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 삼암각(三暗刻) - 암각 3개
    public static boolean isSanankou(HandState hand) {
        Map<String, Integer> counts = new HashMap<>();
        for (String t : hand.tiles) counts.put(t, counts.getOrDefault(t, 0) + 1);

        int ankouCount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() >= 3) {
                ankouCount++;
            }
        }
        return ankouCount >= 3;
    }

    // 삼간쓰(三槓子) - 깡 3개
    public static boolean isSankantsu(HandState hand) {
        Map<String, Integer> counts = new HashMap<>();
        for (String t : hand.tiles) counts.put(t, counts.getOrDefault(t, 0) + 1);

        int kantsuCount = 0;
        for (int count : counts.values()) {
            if (count == 4) kantsuCount++;
        }
        return kantsuCount >= 3;
    }

    // 혼로우토(混老頭) - 1/9/자패로만 + 각자 4개
    public static boolean isHonroto(HandState hand) {
        return isHonroutou(hand) && isToitoi(hand);
    }

    // [기존 함수들 계속...]
    public static boolean isSanshokuDoujun(HandState hand) {
        List<String> man = new ArrayList<>(), pin = new ArrayList<>(), sou = new ArrayList<>();
        for (String t : hand.tiles) {
            if (t.endsWith("m")) man.add(t);
            if (t.endsWith("p")) pin.add(t);
            if (t.endsWith("s")) sou.add(t);
        }
        for (int i = 1; i <= 7; i++) {
            String a = i + "m", b = (i+1) + "m", c = (i+2) + "m";
            String d = i + "p", e = (i+1) + "p", f = (i+2) + "p";
            String g = i + "s", h = (i+1) + "s", k = (i+2) + "s";
            if (man.contains(a) && man.contains(b) && man.contains(c) &&
                    pin.contains(d) && pin.contains(e) && pin.contains(f) &&
                    sou.contains(g) && sou.contains(h) && sou.contains(k))
                return true;
        }
        return false;
    }

    public static boolean isIkkitsuukan(HandState hand) {
        for (char suit : new char[]{'m', 'p', 's'}) {
            boolean[] found = new boolean[10];
            for (String t : hand.tiles) {
                if (t.endsWith(String.valueOf(suit))) {
                    int num = t.charAt(0) - '0';
                    found[num] = true;
                }
            }
            boolean has123 = found[1] && found[2] && found[3];
            boolean has456 = found[4] && found[5] && found[6];
            boolean has789 = found[7] && found[8] && found[9];
            if (has123 && has456 && has789) return true;
        }
        return false;
    }

    public static boolean isJunchanMeldSearch(List<String> tiles) {
        for (String t : tiles) if (isHonor(t)) return false;
        Map<String, Integer> counts = new HashMap<>();
        for (String t : tiles) counts.put(t, counts.getOrDefault(t, 0) + 1);
        for (String pair : counts.keySet()) {
            if (!isTerminal(pair)) continue;
            if (counts.get(pair) >= 2) {
                Map<String, Integer> tmp = new HashMap<>(counts);
                tmp.put(pair, tmp.get(pair) - 2);
                if (isJunchanMelds(tmp, 4)) return true;
            }
        }
        return false;
    }

    private static boolean isJunchanMelds(Map<String, Integer> pool, int left) {
        if (left == 0) {
            int sum = 0;
            for (int v : pool.values()) sum += v;
            return sum == 0;
        }
        for (String t : pool.keySet()) {
            if (!isTerminal(t)) continue;
            if (pool.get(t) >= 3) {
                Map<String, Integer> tmp = new HashMap<>(pool);
                tmp.put(t, tmp.get(t) - 3);
                if (isJunchanMelds(tmp, left - 1)) return true;
            }
        }
        for (String suit : Arrays.asList("m", "p", "s")) {
            for (int i : Arrays.asList(1,7)) {
                String a = i + suit, b = (i+1) + suit, c = (i+2) + suit;
                if (pool.getOrDefault(a,0)>0 && pool.getOrDefault(b,0)>0 && pool.getOrDefault(c,0)>0) {
                    Map<String, Integer> tmp = new HashMap<>(pool);
                    tmp.put(a, tmp.get(a) - 1);
                    tmp.put(b, tmp.get(b) - 1);
                    tmp.put(c, tmp.get(c) - 1);
                    if (isJunchanMelds(tmp, left - 1)) return true;
                }
            }
        }
        return false;
    }

    public static boolean isChantaMeldSearch(List<String> tiles) {
        Map<String, Integer> counts = new HashMap<>();
        for (String t : tiles) counts.put(t, counts.getOrDefault(t, 0) + 1);
        for (String pair : counts.keySet()) {
            if (counts.get(pair) >= 2) {
                Map<String, Integer> tmp = new HashMap<>(counts);
                tmp.put(pair, tmp.get(pair) - 2);
                if (isChantaMelds(tmp, 4)) return true;
            }
        }
        return false;
    }

    private static boolean isChantaMelds(Map<String, Integer> pool, int left) {
        if (left == 0) {
            int sum = 0;
            for (int v : pool.values()) sum += v;
            return sum == 0;
        }
        for (String t : pool.keySet()) {
            if (pool.get(t) >= 3 && isYaochu(t)) {
                Map<String, Integer> tmp = new HashMap<>(pool);
                tmp.put(t, tmp.get(t) - 3);
                if (isChantaMelds(tmp, left - 1)) return true;
            }
        }
        for (String suit : Arrays.asList("m", "p", "s")) {
            for (int i = 1; i <= 7; i++) {
                String a = i + suit, b = (i+1) + suit, c = (i+2) + suit;
                if (pool.getOrDefault(a,0)>0 && pool.getOrDefault(b,0)>0 && pool.getOrDefault(c,0)>0) {
                    if (!(a.startsWith("1")||b.startsWith("1")||c.startsWith("1")||
                            a.startsWith("9")||b.startsWith("9")||c.startsWith("9")||
                            isHonor(a)||isHonor(b)||isHonor(c))) continue;
                    Map<String, Integer> tmp = new HashMap<>(pool);
                    tmp.put(a, tmp.get(a) - 1);
                    tmp.put(b, tmp.get(b) - 1);
                    tmp.put(c, tmp.get(c) - 1);
                    if (isChantaMelds(tmp, left - 1)) return true;
                }
            }
        }
        return false;
    }

    public static boolean isChinitsu(HandState hand) {
        char suit = 0;
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                if (suit == 0) suit = t.charAt(1);
                else if (suit != t.charAt(1)) return false;
            } else {
                return false;
            }
        }
        return suit != 0;
    }

    public static boolean isHonitsu(HandState hand) {
        boolean hasSuit = false, hasHonor = false;
        char suit = 0;
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                hasSuit = true;
                if (suit == 0) suit = t.charAt(1);
                else if (suit != t.charAt(1)) return false;
            } else {
                hasHonor = true;
            }
        }
        return hasSuit && hasHonor;
    }

    public static boolean isHonroutou(HandState hand) {
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                char num = t.charAt(0);
                if (num != '1' && num != '9') return false;
            } else if (!isHonor(t)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTanyao(HandState hand) {
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                char num = t.charAt(0);
                if (num == '1' || num == '9') return false;
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean isSanshokuDokko(HandState hand) {
        int[][] count = new int[3][10];
        for (String t : hand.tiles) {
            int num = t.charAt(0) - '0';
            if (t.endsWith("m")) count[0][num]++;
            if (t.endsWith("p")) count[1][num]++;
            if (t.endsWith("s")) count[2][num]++;
        }
        for (int i = 1; i <= 9; i++) {
            if (count[0][i] >= 2 && count[1][i] >= 2 && count[2][i] >= 2) return true;
        }
        return false;
    }

    public static boolean isToitoi(HandState hand) {
        Map<String, Integer> count = new HashMap<>();
        for (String t : hand.tiles) {
            count.put(t, count.getOrDefault(t, 0) + 1);
        }

        int pair = 0;
        int set = 0;

        for (int c : count.values()) {
            if (c == 2) {
                pair++;
            } else if (c == 3 || c == 4) {
                set++;
            } else {
                return false;
            }
        }
        return pair == 1 && set == 4;
    }

    public static boolean isYakuhai(HandState hand) {
        Map<String, Integer> counts = new HashMap<>();
        for (String t : hand.tiles) {
            counts.put(t, counts.getOrDefault(t, 0) + 1);
        }
        for (String honor : Arrays.asList("P","F","C","E","S","W","N")) {
            if (counts.getOrDefault(honor, 0) >= 3) return true;
        }
        return false;
    }

    public static boolean isShousangen(HandState hand) {
        int P = 0, F = 0, C = 0;
        for (String t : hand.tiles) {
            if (t.equals("P")) P++;
            if (t.equals("F")) F++;
            if (t.equals("C")) C++;
        }
        int pair = 0, pon = 0;
        if (P == 2) pair++; else if (P == 3) pon++;
        if (F == 2) pair++; else if (F == 3) pon++;
        if (C == 2) pair++; else if (C == 3) pon++;
        return (pon == 2 && pair == 1);
    }

    // 헬퍼 함수들
    private static boolean isTerminal(String t) {
        return t.length() == 2 && (t.charAt(0) == '1' || t.charAt(0) == '9');
    }

    private static boolean isHonor(String t) {
        return t.equals("E") || t.equals("S") || t.equals("W") || t.equals("N")
                || t.equals("P") || t.equals("F") || t.equals("C");
    }

    private static boolean isYaochu(String t) {
        return isHonor(t) || (t.length() == 2 && (t.charAt(0) == '1' || t.charAt(0) == '9'));
    }
}