package kr.ac.mjc.majang;

import java.util.*;
import kr.ac.mjc.majang.YakuChecker;

public class MahjongScoreCalculator {

    public static class Result {
        public int totalScore;
        public List<String> yakuList = new ArrayList<>();
        public int han;
        public int fu;
        public String scoreGrade;
    }

    public static Result calculate(HandState hand) {
        Result result = new Result();

        // === 1. 역만(야쿠만) 자동 체크 ===
        List<String> yakuman = YakumanChecker.getYakumanList(hand.tiles, null); // winTile은 필요시 전달
        if (yakuman != null && !yakuman.isEmpty()) {
            result.yakuList.addAll(yakuman);
            result.han = 13 * yakuman.size();
            result.scoreGrade = "역만";
            result.totalScore = hand.isDealer ? 48000 * yakuman.size() : 32000 * yakuman.size();
            return result;
        }

        // === 2. 일반 역 체크 ===
        List<YakuChecker.YakuResult> autoYaku = YakuChecker.getYakuList(hand);
        for (YakuChecker.YakuResult y : autoYaku) {
            result.yakuList.add(y.name);
            result.han += y.han;
        }
        if (hand.yakuList != null) for (String y : hand.yakuList) {
            if (!result.yakuList.contains(y)) result.yakuList.add(y);
        }

        result.fu = hand.fu;

        int han = result.han + hand.dora;
        int fu = hand.fu;
        String grade = "";
        int totalScore = 0;
        int base = 0;

        if (han >= 13) {
            grade = "헤아림 역만";
            totalScore = hand.isDealer ? 48000 : 32000;
        } else if (han >= 11) {
            grade = "삼배만";
            totalScore = hand.isDealer ? 36000 : 24000;
        } else if (han >= 8) {
            grade = "배만";
            totalScore = hand.isDealer ? 24000 : 16000;
        } else if (han >= 6) {
            grade = "하네만";
            totalScore = hand.isDealer ? 18000 : 12000;
        } else if (han == 5 || (han >= 4 && fu >= 40) || (han >= 3 && fu >= 70)) {
            grade = "만관";
            totalScore = hand.isDealer ? 12000 : 8000;
        } else {
            base = fu * (1 << (han + 2));
            base = Math.min(base, 2000);
            if (hand.isTsumo) {
                totalScore = hand.isDealer ? base * 2 * 3 : base * 2 + base;
            } else {
                totalScore = hand.isDealer ? base * 6 : base * 4;
            }
            totalScore = ((totalScore + 99) / 100) * 100;
        }

        result.scoreGrade = grade;
        result.totalScore = totalScore;

        return result;
    }
}
