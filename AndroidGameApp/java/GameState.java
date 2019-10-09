package amai.box_world1;

import java.util.ArrayList;

/**
 * gcd を扱う処理はここで実装する
 * 子stateでは extFileData特有の処理のみ記述する
 */

public class GameState {

    //ゲーム内共通データ
    CommonData gcd = null;

    // State内データ
    String localState = "";
    String actText = "";     // Initial, ReStart, btnText, AsyncExe
    // 初実行、再実行前処理、ボタンの文字列、バトル実行

    //表示データ
    String mainImg = "";
    String objectImg = "";
    String personImg = "";
    String chatImg = "";
    String mainText = "";
    String[] btnTexts = {"", "", "", "", ""};
    // 特殊表示フラグ
    //String statusDispFlag = "";   // battle   //バトル用ステータス表示
    String btnAbleFlag = "";      // wait, onlyRtn     //ボタンを押せなくして handleMessageを起動する, 戻るボタンのみ使用可能にする
    String canvasFlag = "";       //
    int loopDelayTime = 500;      // handleMessageのdelay時間

    String bgm = "";// 音楽
    int bgmRePos = 0;


    //コンストラクタ
    GameState() {
    }


    //初期化
    void init(CommonData cd, ArrayList<ArrayList<String>> extFileData) {
    }

    // 処理する
    void sExec(String btnText) {
    }

    //再実行 前処理
    void sReStart(String actText) {
    }

    // ループ処理
    void sLoopExec(String actText) {
    }


    //====================共通処理の実装=============================================

    //ステータス欄の表示内容を返す
    //String getStatusText() {return null;}
    //----------ステータス表示作成-------------
    String getStatusText() {
        String sts = "";

        // 戦闘説明
        if(gcd.intGFlag.containsKey("戦闘数値説明")) {
            if (gcd.intGFlag.get("戦闘数値説明") > 0) {
                sts += "HP 80/100";
                sts += "  速さ5";
                sts += "\n行動 ";
                sts += "ロングソード";
                sts += "\n威力32";
                sts += "  防御12";
                sts += "\nウェイト 23";

                sts += "\n\nノア";
                sts += "\nHP 50";
                sts += "     速さ5";
                sts += "\n行動 ";
                sts += "ヒート";
                sts += "\n威力20";
                sts += "  防御0";
                sts += "  貫通";  // 貫通など特殊効果の記述
                sts += "\nウェイト 18";
                return sts;
            }
        }

        // 通常時
        //sts += gcd.paragraph;
        //sts += "\nメンバー";
        for (int i = 0; i < gcd.charaMem.size(); i++) {
            Character chara = gcd.charaMem.get(i);
            //sts += "\n" + chara.name;
            //sts += "\n\nHP " + chara.nHP + "/" + chara.maxHP;
        }
        sts += "結晶 " + gcd.money + " 欠片";
        return sts;
    }


    // local値判定 -----------------------------------------------
    boolean checkLocalState(String fileLocal) {
        if (fileLocal.equals(localState) || fileLocal.length() == 0) {
            return true;
        }
        return false;
    }

    // btn判定
    boolean checkBtnAction(String fileAct) {
        if (fileAct.equals(actText) || fileAct.length() == 0) {
            return true;
        }
        return false;
    }
}
