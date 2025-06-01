package kr.ac.mjc.majang;

import java.util.*;

/**
 * 일반 역(야쿠) 판정, 판수(한수) 계산 담당 클래스
 * hand(HandState) 상태를 기준으로 각 역별 적용 여부/한수 반환
 */
public class YakuChecker {

    /**
     * 역/한수 반환용 내부 구조체
     */
    public static class YakuResult {
        public String name;
        public int han;
        public YakuResult(String name, int han) {
            this.name = name;
            this.han = han;
        }
    }

    /**
     * 현재 hand 상태에서 적용 가능한 모든 일반 역(야쿠)과 판수 반환
     */
    public static List<YakuResult> getYakuList(HandState hand) {
        List<YakuResult> yaku = new ArrayList<>();

        // === 멘젠 한정(후로 시 불가) ===
        if (hand.isMenzen) {
            if (isRiichi(hand)) yaku.add(new YakuResult("리치", 1));
            if (isPinfu(hand)) yaku.add(new YakuResult("핑후", 1));
            if (isIppatsu(hand)) yaku.add(new YakuResult("일발", 1));
            if (isChitoitsu(hand)) yaku.add(new YakuResult("치또이", 2));
            if (isRyanpeko(hand)) yaku.add(new YakuResult("량페코", 3));
        }

        // === 멘젠/후로 모두 가능, 판수 다름 ===
        if (isSanshokuDoujun(hand)) yaku.add(new YakuResult("삼색동순", hand.isMenzen ? 2 : 1));
        if (isIkkitsuukan(hand)) yaku.add(new YakuResult("일기통관", hand.isMenzen ? 2 : 1));
        if (isJunchan(hand)) yaku.add(new YakuResult("준찬타", hand.isMenzen ? 2 : 1));
        if (isChanta(hand)) yaku.add(new YakuResult("찬타", hand.isMenzen ? 2 : 1));
        if (isChinitsu(hand)) yaku.add(new YakuResult("청일색", hand.isMenzen ? 6 : 5));
        if (isHonitsu(hand)) yaku.add(new YakuResult("혼일색", hand.isMenzen ? 3 : 2));
        if (isHonroutou(hand)) yaku.add(new YakuResult("혼노두", 2)); // 멘젠/후로 구분 없음
        if (isTanyao(hand)) yaku.add(new YakuResult("탕야오", 1));    // 무조건 1판

        // === 후로도 무조건 1판 유지 ===
        if (isYakuhai(hand)) yaku.add(new YakuResult("역패", 1));
        if (isSanshokuDokko(hand)) yaku.add(new YakuResult("삼색동각", 2));
        if (isShousangen(hand)) yaku.add(new YakuResult("소삼원", 2));

        return yaku;
    }

    // ==== 역별 판정 함수 ====

    // 리치(멘젠 한정)
    public static boolean isRiichi(HandState hand) {
        return hand.isMenzen;
    }

    // 일발(멘젠 한정)
    public static boolean isIppatsu(HandState hand) {
        // (1) isIppatsu 필드가 있으면
        // return hand.isIppatsu;

        // (2) yakuList에 "일발"이 포함되어 있으면
        return hand.yakuList != null && hand.yakuList.contains("일발");
    }


    // 핑후(멘젠 한정, 모든 멘츠가 슌츠, 헤드는 역패/자패 아님, 양면 대기)
    public static boolean isPinfu(HandState hand) {
        // 1. 멘젠 아니면 무조건 불가
        if (!hand.isMenzen) return false;
        List<String> tiles = hand.tiles;
        if (tiles == null || tiles.size() != 14) return false;

        // 2. 멘츠 분해 (여기선 간단히 순차적으로 4셋+1쌍 조합 찾기)
        // 모든 3개 조합이 슌츠(차/순자)인지 체크
        Map<String, Integer> counts = new HashMap<>();
        for (String t : tiles) counts.put(t, counts.getOrDefault(t, 0) + 1);

        // (1) 모든 조합 중에서 1쌍(또이츠) & 나머지는 모두 슌츠로 분해되는지 체크
        // (이 부분이 마작 AI/계산기의 본질, 여기선 단순화: 자주 쓰는 정석 로직 차용)
        List<String> pairs = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() >= 2) pairs.add(e.getKey());
        }

        // 핑후는 반드시 또이츠가 1개만 존재해야 함
        boolean validPinfu = false;
        for (String pair : pairs) {
            // 2개 빼고 나머지로 멘츠 분해(3개씩 4조)
            Map<String, Integer> tempCounts = new HashMap<>(counts);
            tempCounts.put(pair, tempCounts.get(pair) - 2);

            List<List<String>> melds = new ArrayList<>();
            int meldCount = 0;
            // 4조 멘츠(슈운츠) 분해 시도
            while (meldCount < 4) {
                boolean found = false;
                // 슌츠(차)만 허용: nX, n+1X, n+2X
                for (char suit : new char[]{'m', 'p', 's'}) {
                    for (int n = 1; n <= 7; n++) {
                        String a = n + String.valueOf(suit);
                        String b = (n+1) + String.valueOf(suit);
                        String c = (n+2) + String.valueOf(suit);
                        if (tempCounts.getOrDefault(a,0) >= 1 &&
                                tempCounts.getOrDefault(b,0) >= 1 &&
                                tempCounts.getOrDefault(c,0) >= 1) {
                            // 멘츠 발견 → 빼기
                            tempCounts.put(a, tempCounts.get(a)-1);
                            tempCounts.put(b, tempCounts.get(b)-1);
                            tempCounts.put(c, tempCounts.get(c)-1);
                            melds.add(Arrays.asList(a,b,c));
                            found = true;
                            meldCount++;
                            break;
                        }
                    }
                    if (found) break;
                }
                if (!found) break;
            }
            // 네 개 모두 슌츠로 분해됐으면
            if (meldCount == 4) {
                // 3. 헤드가 역패/자패(z1~z7)가 아닌가? (단순히 z로 시작)
                if (pair.startsWith("z")) continue;
                // 4. 대기가 양면 대기(이 부분은 hand에 winTile 등 추가 정보 있어야 완벽히 구현 가능, 여기선 스킵)
                // 실제 게임에선 승패타 정보(winning tile, winTile)가 꼭 필요함
                validPinfu = true;
                break;
            }
        }
        return validPinfu;
    }


    // 치또이(멘젠 한정, 7쌍)
    public static boolean isChitoitsu(HandState hand) {
        if (!hand.isMenzen) return false;
        Map<String, Integer> map = new HashMap<>();
        for (String t : hand.tiles) map.put(t, map.getOrDefault(t, 0) + 1);
        int pair = 0;
        for (int v : map.values()) if (v == 2) pair++;
        return pair == 7;
    }

    // 량페코(멘젠 한정, 실제론 멘츠 분해 필요)
    public static boolean isRyanpeko(HandState hand) {
        if (!hand.isMenzen) return false;
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

    // 준찬타(멘젠 2판, 후로 1판)
    public static boolean isJunchan(HandState hand) {
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                char num = t.charAt(0);
                if (num != '1' && num != '9') return false;
            } else {
                return false;
            }
        }
        return true;
    }

    // 찬타(멘젠 2판, 후로 1판)
    public static boolean isChanta(HandState hand) {
        boolean hasYaochu = false;
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                char num = t.charAt(0);
                if (num == '1' || num == '9') hasYaochu = true;
            } else {
                hasYaochu = true;
            }
        }
        return hasYaochu;
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

    // 혼노두(2판, 멘젠/후로 동일, 1/9/자패로만)
    public static boolean isHonroutou(HandState hand) {
        for (String t : hand.tiles) {
            if (t.endsWith("m") || t.endsWith("p") || t.endsWith("s")) {
                char num = t.charAt(0);
                if (num != '1' && num != '9') return false;
            }
        }
        return true;
    }

    // 탕야오(무조건 1판)
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

    // 역패(무조건 1판, 자패 3개 이상)
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
