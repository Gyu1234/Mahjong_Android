package kr.ac.mjc.majang;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import java.util.*;

public class ConditionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition);

        // 선택된 패 리스트 받기
        ArrayList<String> tiles = getIntent().getStringArrayListExtra("tiles");

        // UI 컴포넌트 연결
        CheckBox checkFuro = findViewById(R.id.checkFuro);
        RadioGroup radioGroupWinType = findViewById(R.id.radioGroupWinType);
        RadioButton radioTsumo = findViewById(R.id.radioTsumo);
        Switch switchDealer = findViewById(R.id.switchDealer); //오야 선택
        EditText editDora = findViewById(R.id.editDora);
        EditText editFu = findViewById(R.id.editFu);

        // 역 체크박스
        CheckBox checkRiichi = findViewById(R.id.checkRiichi);
        CheckBox checkIppatsu = findViewById(R.id.checkIppatsu);
        CheckBox checkHaitei = findViewById(R.id.checkHaitei);

        Button btnCalculate = findViewById(R.id.btnCalculate);

        // ---- [추가] 일발/하저로어 배열로 관리 ----
        CheckBox[] yakuChecks = { checkIppatsu, checkHaitei };

        // ---- [핵심!] 리치/쯔모 활성화 연동 ----
        Runnable updateYakuEnable = () -> {
            boolean enable = checkRiichi.isChecked() || radioTsumo.isChecked();
            for (CheckBox yakuCheck : yakuChecks) {
                yakuCheck.setEnabled(enable);
                if (!enable) yakuCheck.setChecked(false); // 비활성화시 체크 해제
            }
        };

        // 리치 체크 변화 리스너
        checkRiichi.setOnCheckedChangeListener((buttonView, isChecked) -> updateYakuEnable.run());

        // 쯔모 라디오 선택 리스너
        radioGroupWinType.setOnCheckedChangeListener((group, checkedId) -> updateYakuEnable.run());

        // 최초 진입시에도 상태 동기화
        updateYakuEnable.run();

        btnCalculate.setOnClickListener(v -> {
            // ★ 조건 선택 체크 (최소 한 개 이상)
            boolean isAnyCondition =
                    checkFuro.isChecked()
                            || radioGroupWinType.getCheckedRadioButtonId() != -1
                            || switchDealer.isChecked()
                            || !editDora.getText().toString().trim().isEmpty()
                            || !editFu.getText().toString().trim().isEmpty()
                            || checkRiichi.isChecked()
                            || checkIppatsu.isChecked()
                            || checkHaitei.isChecked();

            if (!isAnyCondition) {
                Toast.makeText(this, "조건을 한 개 이상 선택하거나 입력하세요.", Toast.LENGTH_SHORT).show();
                return; // ResultActivity로 이동하지 않음
            }

            // HandState 객체 생성 및 값 채우기
            HandState hand = new HandState();
            hand.tiles = tiles;
            hand.isFuro = checkFuro.isChecked();
            hand.isMenzen = !checkFuro.isChecked(); // 오픈(후로)이면 멘젠 false

            int selectedId = radioGroupWinType.getCheckedRadioButtonId();
            hand.isTsumo = (selectedId == R.id.radioTsumo);
            hand.isDealer = switchDealer.isChecked();
            hand.dora = getInt(editDora, 0);
            hand.fu = getInt(editFu, 20);

            hand.yakuList = new ArrayList<>();
            if (checkRiichi.isChecked()) hand.yakuList.add("리치");
            if (checkIppatsu.isChecked()) hand.yakuList.add("일발");
            if (checkHaitei.isChecked()) hand.yakuList.add("하저로어");
            // 추가 역이 있다면 여기에 계속...

            // ResultActivity로 이동 (핸드 데이터 전달)
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("hand", hand);
            startActivity(intent);
        });
    }

    // 문자열 -> int 변환 유틸 (실패시 def 반환)
    private int getInt(EditText edit, int def) {
        try {
            return Integer.parseInt(edit.getText().toString().trim());
        } catch (Exception e) {
            return def;
        }
    }
}
