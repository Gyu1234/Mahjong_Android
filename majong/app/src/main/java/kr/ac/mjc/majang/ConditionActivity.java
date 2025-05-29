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

        ArrayList<String> tiles = getIntent().getStringArrayListExtra("tiles");

        CheckBox checkFuro = findViewById(R.id.checkFuro);
        RadioGroup radioGroupWinType = findViewById(R.id.radioGroupWinType);
        Switch switchDealer = findViewById(R.id.switchDealer);
        EditText editDora = findViewById(R.id.editDora);
        EditText editFu = findViewById(R.id.editFu);
        CheckBox checkRiichi = findViewById(R.id.checkRiichi);
        CheckBox checkIppatsu = findViewById(R.id.checkIppatsu);
        CheckBox checkHaitei = findViewById(R.id.checkHaitei);

        Button btnCalculate = findViewById(R.id.btnCalculate);

        btnCalculate.setOnClickListener(v -> {
            HandState hand = new HandState();
            hand.tiles = tiles;
            hand.isFuro = checkFuro.isChecked();
            hand.isMenzen = !checkFuro.isChecked(); // 오픈(후로)이면 멘젠 false

            int selectedId = radioGroupWinType.getCheckedRadioButtonId();
            hand.isTsumo = selectedId == R.id.radioTsumo;
            hand.isDealer = switchDealer.isChecked();
            hand.dora = getInt(editDora, 0);
            hand.fu = getInt(editFu, 20);

            hand.yakuList = new ArrayList<>();
            if (checkRiichi.isChecked()) hand.yakuList.add("리치");
            if (checkIppatsu.isChecked()) hand.yakuList.add("일발");
            if (checkHaitei.isChecked()) hand.yakuList.add("하저로어");

            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("hand", hand);
            startActivity(intent);
        });
    }

    private int getInt(EditText edit, int def) {
        try {
            return Integer.parseInt(edit.getText().toString().trim());
        } catch (Exception e) {
            return def;
        }
    }
}
