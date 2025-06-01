package kr.ac.mjc.majang;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import java.util.*;

public class ResultActivity extends Activity {

    // 수패/자패 문자열 → 이미지 리소스 id 매핑 함수
    private int getTileDrawableRes(String tile) {
        switch(tile) {
            case "1m": return R.drawable.m1;
            case "2m": return R.drawable.m2;
            case "3m": return R.drawable.m3;
            case "4m": return R.drawable.m4;
            case "5m": return R.drawable.m5;
            case "6m": return R.drawable.m6;
            case "7m": return R.drawable.m7;
            case "8m": return R.drawable.m8;
            case "9m": return R.drawable.m9;
            case "1p": return R.drawable.t1;
            case "2p": return R.drawable.t2;
            case "3p": return R.drawable.t3;
            case "4p": return R.drawable.t4;
            case "5p": return R.drawable.t5;
            case "6p": return R.drawable.t6;
            case "7p": return R.drawable.t7;
            case "8p": return R.drawable.t8;
            case "9p": return R.drawable.t9;
            case "1s": return R.drawable.s1;
            case "2s": return R.drawable.s2;
            case "3s": return R.drawable.s3;
            case "4s": return R.drawable.s4;
            case "5s": return R.drawable.s5;
            case "6s": return R.drawable.s6;
            case "7s": return R.drawable.s7;
            case "8s": return R.drawable.s8;
            case "9s": return R.drawable.s9;
            // 자패(동, 남, 서, 북, 백, 발, 중)
            case "E": return R.drawable.z1; // 동
            case "S": return R.drawable.z2; // 남
            case "W": return R.drawable.z3; // 서
            case "N": return R.drawable.z4; // 북
            case "P": return R.drawable.z5; // 백(흰)
            case "F": return R.drawable.z6; // 발(초록)
            case "C": return R.drawable.z7; // 중(빨강)
        }
        return R.drawable.back; // 예외처리: 알 수 없는 타일은 뒷면
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 결과 UI 요소 찾기
        TextView textScore = findViewById(R.id.text_score);
        TextView textYaku = findViewById(R.id.text_yaku);
        TextView textGrade = findViewById(R.id.text_score_grade);
        Button buttonBack = findViewById(R.id.button_back);
        LinearLayout row1 = findViewById(R.id.hand_container_row1);
        LinearLayout row2 = findViewById(R.id.hand_container_row2);

        // 인텐트로부터 HandState 객체 받아오기
        HandState hand = (HandState) getIntent().getSerializableExtra("hand");

        // 패 리스트 가져오기 (null 방지)
        List<String> tiles = (hand != null && hand.tiles != null) ? hand.tiles : new ArrayList<>();

        // 기존 뷰 초기화(패 남는 버그 방지)
        row1.removeAllViews();
        row2.removeAllViews();

        // 0~6번 패는 row1, 7~13번 패는 row2에 표시
        for (int i = 0; i < tiles.size(); i++) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(80, 120);
            lp.setMargins(4, 0, 4, 0);
            iv.setLayoutParams(lp);
            iv.setImageResource(getTileDrawableRes(tiles.get(i)));
            if (i < 7) row1.addView(iv);
            else row2.addView(iv);
        }

        // -------------------------
        // 점수/역/등급 계산 및 출력
        // -------------------------

        // 1. 역만(야쿠만) 판정부터 우선

        List<String> yakumanList = YakumanChecker.getYakumanList(tiles, null, hand != null && hand.isMenzen);
        if (yakumanList != null && !yakumanList.isEmpty()) {
            // 역만 핸드인 경우
            int baseScore = (hand != null && hand.isDealer) ? 48000 : 32000; // <== 이 한 줄 추가!
            int yakumanScore = baseScore * yakumanList.size();
            String oyaString = (hand != null && hand.isDealer) ? " (오야)" : "";
            textScore.setText("점수: " + yakumanScore + oyaString);
            textYaku.setText("역: " + String.join(", ", yakumanList));
            textGrade.setText("등급: " + (yakumanList.size() == 1 ? "역만" : yakumanList.size() + "배 역만"));
            return;
        }


        else {
            // 2. 일반 패는 점수/역 계산 결과 사용
            MahjongScoreCalculator.Result result = MahjongScoreCalculator.calculate(hand);
            String oyaString = (hand != null && hand.isDealer) ? " (오야)" : "";
            textScore.setText("점수: " + result.totalScore + oyaString);

            List<String> yakuList = (result.yakuList != null) ? result.yakuList : new ArrayList<>();

            // === '역 없음' 처리 추가 ===
            String yakuText;
            if (yakuList.isEmpty()) {
                yakuText = "역: 없음";   // 역 없음 출력
            } else {
                yakuText = "역: " + String.join(", ", yakuList);
            }
            textYaku.setText(yakuText);

            // 등급도 마찬가지로 비어 있으면 '일반' 표시
            String gradeString = (result.scoreGrade != null && !result.scoreGrade.isEmpty()) ?
                    result.scoreGrade : "일반";
            textGrade.setText("등급: " + gradeString);
        }

        // -------------------------
        // 돌아가기(메인으로) 버튼 처리
        // -------------------------
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
