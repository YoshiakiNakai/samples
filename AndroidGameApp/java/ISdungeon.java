package amai.box_world1;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Nakai Yoshiaki on 2017/12/01.
 */

public class ISdungeon extends GameState {


    //外部ファイルデータ
    ArrayList<String> actionList = new ArrayList<>();
    ArrayList<String> situationList = new ArrayList<>();

    //初期化
    @Override
    void init(CommonData cd, ArrayList<ArrayList<String>> extFileData){
        this.actionList = extFileData.get(0);       //外部データの受け取り
        this.situationList = extFileData.get(1);
        this.gcd = cd;                  //共通データの受け取り
        localState = gcd.tmpLocal;  // localStateの受け取り

        bgm = "city";
        bgmRePos = 0;
        //this.canvasFlag = "";
    };

    //コンストラクタ
    public ISdungeon(){
    }

    // 実行   ------------------------------------------------
    @Override
    public void sExec(String btnText){
        if(btnText.length()!=0) this.actText = btnText;     //行動を保存

        judgeAction();  // 行動判定
        selectSituation();  // 次の状況選択
        return;
    }

    // 再実行 前処理 ------------------------------------------------
    @Override
    public void sReStart(String btnText) {
        switch (gcd.tmpLocal) {
            case ("win"):
                selectSituation();  // 状況選択
                gcd.tmpLocal = "";
                break;
            case ("lose"):
                selectSituation();  // 状況選択
                gcd.tmpLocal = "";
                break;
            case ("escape"):
                selectSituation();  // 状況選択
                gcd.tmpLocal = "";
                break;
            case("reload"):
                selectSituation();  // 状況選択
                gcd.tmpLocal = "";
                break;
        }
        return;
    }


    // 行動判定関数   ------------------------------------------------
    void judgeAction(){
        for(int i=0; i<actionList.size(); i++) {
            //actionファイルを一行ずつチェックしていく
            String fileInfo[] = actionList.get(i).split(",");
            if(checkLocalState(fileInfo[0]) == false) continue;  //local判定
            if(checkBtnAction(fileInfo[1]) == false) continue;  //btn判定
            if(gcd.checkGlobalFlag(fileInfo[2]) == false) continue;    //Flag判定

            //処理
            localState = fileInfo[3];
            gcd.nextState = fileInfo[4];
            gcd.tmpLocal = fileInfo[5];
            gcd.extFileNames = fileInfo[6];

            gcd.updateGlobalFlag(fileInfo[7]);

            return;
        }
    }

    // 状況判定関数 ------------------------------------------------
    void selectSituation(){
        // situationファイルを処理する
        for (int i = 0; i < situationList.size(); i++) {
            String fileInfo[] = situationList.get(i).split(",");
            if(checkLocalState(fileInfo[0]) == false) continue;
            if (gcd.checkGlobalFlag(fileInfo[1]) == false) continue;

            //処理
            mainImg = fileInfo[2];
            objectImg = fileInfo[3];
            personImg = fileInfo[4];
            mainText = gcd.raw2message( fileInfo[5]);

            btnTexts[0] = fileInfo[6];
            btnTexts[1] = fileInfo[7];
            btnTexts[2] = fileInfo[8];
            btnTexts[3] = fileInfo[9];
            btnTexts[4] = fileInfo[10];

            gcd.updateGlobalFlag(fileInfo[11]);

            return;
        }
    }

}
