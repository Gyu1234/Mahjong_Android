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
     * 치또이/량페코, 혼노두/준찬타/찬타 배타 처리 적용,
     * 판수 큰 쪽 우선, 나머지 역 중복 허용
     */
    public static List<YakuResult> getYakuList(HandState hand) {
        List<YakuResult> yaku = new ArrayList<>();

        // [1] 치또이/량페코 (배타, 큰 쪽 우선, 자패 포함 시 치또이만 성립)
        boolean chitoitsu = isChitoitsu(hand);
        boolean ryanpeko = isRyanpeko(hand);
        if (ryanpeko) {
            yaku.add(new YakuResult("량페코", 3));
        } else if (chitoitsu) {
            yaku.add(new YakuResult("치또이", 2));
        }

        // [2] 혼노두/준찬타/찬타 (배타, 큰 쪽 우선)
        int honroutouHan = isHonroutou(hand) ? 2 : 0;
        int junchanHan = isJunchan(hand) ? (hand.isMenzen ? 2 : 1) : 0;
        int chantaHan = isChanta(hand) ? (hand.isMenzen ? 2 : 1) : 0;

        // 판수 큰 것만 남김
        if (honroutouHan >= junchanHan && honroutouHan >= chantaHan && honroutouHan > 0) {
            yaku.add(new YakuResult("혼노두", honroutouHan));
        } else if (junchanHan >= chantaHan && junchanHan > 0) {
            yaku.add(new YakuResult("준찬타", junchanHan));
        } else if (chantaHan > 0) {
            yaku.add(new YakuResult("찬타", chantaHan));
        }

        // [3] 멘젠 한정 역
        if (hand.isMenzen) {
            if (isRiichi(hand)) yaku.add(new YakuResult("리치", 1));
            if (isPinfu(hand)) yaku.add(new YakuResult("핑후", 1));
            if (isTsumo(hand)) yaku.add(new YakuResult("쯔모", 1));
            if (isIppatsu(hand)) yaku.add(new YakuResult("일발", 1));
        }

        // [4] 나머지 중복 허용 역
        if (isSanshokuDoujun(hand)) yaku.add(new YakuResult("삼색동순", hand.isMenzen ? 2 : 1));
        if (isIkkitsuukan(hand)) yaku.add(new YakuResult("일기통관", hand.isMenzen ? 2 : 1));
        if (isChinitsu(hand)) yaku.add(new YakuResult("청일색", hand.isMenzen ? 6 : 5));
        if (isHonitsu(hand)) yaku.add(new YakuResult("혼일색", hand.isMenzen ? 3 : 2));
        if (isYakuhai(hand)) yaku.add(new YakuResult("역패", 1));
        if (isSanshokuDokko(hand)) yaku.add(new YakuResult("삼색동각", 2));
        if (isShousangen(hand)) yaku.add(new YakuResult("소삼원", 2));
        if (isTanyao(hand)) yaku.add(new YakuResult("탕야오", 1));

        return yaku;
    }

    // === 각 역별 판정 함수 ===

    // 리치(멘젠 한정, 버튼 체크된 경우)
    public static boolean isRiichi(HandState hand) {
        return hand.isMenzen && hand.yakuList != null && hand.yakuList.contains("리치");
    }

    // 쯔모(멘젠 한정, 승리타입)
    public static boolean isTsumo(HandState hand) {
        return hand.isMenzen && hand.isTsumo;
    }

    // 일발(멘젠 한정, 버튼 체크된 경우)
    public static boolean isIppatsu(HandState hand) {
        return hand.yakuList != null && hand.yakuList.contains("일발");
    }

    // 핑후(멘젠 한정, 슌츠만, 자패 헤드X, 1쌍)
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
                if (pair.startsWith("z")) continue; // 자패 헤드 불가
                return true;
            }
        }
        return false;
    }

    // 치또이(멘젠 한정, 7쌍, 자패 포함 O)
    public static boolean isChitoitsu(HandState hand) {
        if (!hand.isMenzen) return false;
        Map<String, Integer> map = new HashMap<>();
        for (String t : hand.tiles) map.put(t, map.getOrDefault(t, 0) + 1);
        int pair = 0;
        for (int v : map.values()) if (v == 2) pair++;
        return pair == 7;
    }

    // 량페코(멘젠 한정, 자패 있으면 불가)
    public static boolean isRyanpeko(HandState hand) {
        if (!hand.isMenzen) return false;
        for (String t : hand.tiles) {
            if (isHonor(t)) return false;
        }
        // 실전 구현은 멘츠 분해 필요. 여기선 4쌍 이상(7쌍이 아님)만 대강 체크.
        Map<String, Integer> map = new HashMap<>();
        for (String t : hand.tiles) map.put(t, map.getOrDefault(t, 0) + 1);
        int pairCount = 0;
        for (int v : map.values()) if (v == 2) pairCount++;
        return pairCount >= 4;
    }

    // 삼색동순(멘젠 2판, 후로 1판)
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

    // 일기통관(멘젠 2판, 후로 1판)
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

    // 자패 판정
    private static boolean isHonor(String t) {
        return t.equals("E") || t.equals("S") || t.equals("W") || t.equals("N")
                || t.equals("P") || t.equals("F") || t.equals("C");
    }

    // 준찬타: 모든 멘츠/쌍이 1/9(끝수)만, 자패 포함 X, 2~8 단독 멘츠 없음
    public static boolean isJunchan(HandState hand) {
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                char num = t.charAt(0);
                if (num != '1' && num != '9') return false; // 중간패 있으면 false
            } else {
                return false; // 자패 있으면 false
            }
        }
        return true;
    }

    // 찬타: 1/9/자패는 반드시 하나 이상, 2~8단독 멘츠 3개 이상이면 false
    public static boolean isChanta(HandState hand) {
        boolean hasYaochu = false;
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                char num = t.charAt(0);
                if (num == '1' || num == '9') hasYaochu = true;
            } else if (isHonor(t)) {
                hasYaochu = true;
            }
        }
        if (!hasYaochu) return false;
        int middleTileCount = 0;
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                char num = t.charAt(0);
                if (num >= '2' && num <= '8') middleTileCount++;
            }
        }
        // "중간패"가 많으면 찬타 불가 (대략적 기준)
        if (middleTileCount >= 3) return false;
        return true;
    }

    // 청일색(멘젠 6판, 후로 5판)
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

    // 혼일색(멘젠 3판, 후로 2판)
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

    // 혼노두(2판, 1/9/자패로만 구성)
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

    // 탕야오(1~9/자패 없이 2~8만 사용)
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

    // 삼색동각(무조건 2판)
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

    // 역패(자패가 3개 이상)
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

    // 소삼원(백/발/중 중 2개는 퐁, 1개는 또이츠)
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
}
