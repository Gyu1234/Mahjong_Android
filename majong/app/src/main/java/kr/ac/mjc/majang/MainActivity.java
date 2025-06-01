package kr.ac.mjc.majang;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import java.util.*;

public class MainActivity extends Activity {
    private List<String> selectedTiles = new ArrayList<>();
    private int tilePos = 0;
    private ImageButton[] tileButtons;
    private int tileButtonCount = 14;

    private Map<Integer, String> tileMap = new HashMap<>();
    private int[] selectButtonIds = {
            R.id.b1, R.id.b2, R.id.b3, R.id.b4, R.id.b5, R.id.b6, R.id.b7,
            R.id.b8, R.id.b9, R.id.b10, R.id.b11, R.id.b12, R.id.b13, R.id.b14
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tileButtons = new ImageButton[tileButtonCount];
        for (int i = 0; i < tileButtonCount; i++) {
            tileButtons[i] = findViewById(selectButtonIds[i]);
        }
        TextView tileDisplay = findViewById(R.id.tileDisplay);

        // ID → 코드 매핑
        tileMap.put(R.id.m1, "1m"); tileMap.put(R.id.m2, "2m"); tileMap.put(R.id.m3, "3m");
        tileMap.put(R.id.m4, "4m"); tileMap.put(R.id.m5, "5m"); tileMap.put(R.id.m6, "6m");
        tileMap.put(R.id.m7, "7m"); tileMap.put(R.id.m8, "8m"); tileMap.put(R.id.m9, "9m");
        tileMap.put(R.id.t1, "1p"); tileMap.put(R.id.t2, "2p"); tileMap.put(R.id.t3, "3p");
        tileMap.put(R.id.t4, "4p"); tileMap.put(R.id.t5, "5p"); tileMap.put(R.id.t6, "6p");
        tileMap.put(R.id.t7, "7p"); tileMap.put(R.id.t8, "8p"); tileMap.put(R.id.t9, "9p");
        tileMap.put(R.id.s1, "1s"); tileMap.put(R.id.s2, "2s"); tileMap.put(R.id.s3, "3s");
        tileMap.put(R.id.s4, "4s"); tileMap.put(R.id.s5, "5s"); tileMap.put(R.id.s6, "6s");
        tileMap.put(R.id.s7, "7s"); tileMap.put(R.id.s8, "8s"); tileMap.put(R.id.s9, "9s");
        tileMap.put(R.id.z1, "E"); tileMap.put(R.id.z2, "S"); tileMap.put(R.id.z3, "W");
        tileMap.put(R.id.z4, "N"); tileMap.put(R.id.z5, "P"); tileMap.put(R.id.z6, "F"); tileMap.put(R.id.z7, "C");

        for (Integer id : tileMap.keySet()) {
            ImageButton tileBtn = findViewById(id);
            if (tileBtn != null) {
                tileBtn.setOnClickListener(v -> {
                    String tileCode = tileMap.get(id);

                    // [추가] 이미 선택된 tile 개수 체크
                    int count = 0;
                    for (String t : selectedTiles) {
                        if (t.equals(tileCode)) count++;
                    }
                    if (count >= 4) {
                        Toast.makeText(this, "한 종류의 타일은 최대 4장까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedTiles.size() < tileButtonCount) {
                        selectedTiles.add(tileCode);
                        tileButtons[selectedTiles.size() - 1].setImageDrawable(tileBtn.getDrawable());
                        updateDisplay(tileDisplay);
                    } else {
                        Toast.makeText(this, "최대 14패까지 입력 가능합니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        for (int i = 0; i < tileButtonCount; i++) {
            final int idx = i;
            tileButtons[i].setOnClickListener(v -> {
                if (idx < selectedTiles.size()) {
                    for (int j = idx; j < selectedTiles.size(); j++) {
                        tileButtons[j].setImageResource(R.drawable.back);
                    }
                    selectedTiles = selectedTiles.subList(0, idx);
                    updateDisplay(tileDisplay);
                }
            });
        }
        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if (selectedTiles.size() < 14) {
                Toast.makeText(this, "패를 14장 모두 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ConditionActivity.class);
            intent.putStringArrayListExtra("tiles", new ArrayList<>(selectedTiles));
            startActivity(intent);
        });
    }

    private void updateDisplay(TextView tileDisplay) {
        tileDisplay.setText("선택된 패: " + String.join(" ", selectedTiles));
    }
}
