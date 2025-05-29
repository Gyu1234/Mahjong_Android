package kr.ac.mjc.majang;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;
import java.util.List;

public class ResultActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView textHand = findViewById(R.id.text_hand);
        TextView textScore = findViewById(R.id.text_score);
        TextView textYaku = findViewById(R.id.text_yaku);
        TextView textGrade = findViewById(R.id.text_score_grade);
        Button buttonBack = findViewById(R.id.button_back);

        HandState hand = (HandState) getIntent().getSerializableExtra("hand");
        MahjongScoreCalculator.Result result = MahjongScoreCalculator.calculate(hand);

        String handString = (hand != null && hand.tiles != null)
                ? String.join(" ", hand.tiles)
                : "(입력 없음)";
        textHand.setText("선택된 패: " + handString);

        textScore.setText("점수: " + result.totalScore);

        List<String> yakuList = result.yakuList;
        String yakuText = (yakuList == null || yakuList.isEmpty())
                ? "역: 없음"
                : "역: " + String.join(", ", yakuList);
        textYaku.setText(yakuText);

        if (textGrade != null) {
            String gradeString = result.scoreGrade;
            if (gradeString == null || gradeString.isEmpty()) gradeString = "일반";
            textGrade.setText("등급: " + gradeString);
        }

        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
