package amai.box_world1;

import java.util.ArrayList;

/**
 * Created by Nakai Yoshiaki on 2017/12/02.
 */

public class ISchatEvent extends GameState {

    //外部ファイルデータ
    ArrayList<String> eventList = new ArrayList<>();

    //初期化
    void init(CommonData cd, ArrayList<ArrayList<String>> extFileData){
        this.eventList = extFileData.get(0);       //外部データの受け取り
        this.gcd = cd;                  //共通データの受け取り
        localState = gcd.tmpLocal;  // localStateの受け取り

        //this.canvasFlag = "";
    };

    //コンストラクタ
    public ISchatEvent(){
    }

    // 実行   ------------------------------------------------
    public void sExec(String btnText){
        if(btnText.length()!=0) this.actText = btnText;     //行動を保存

        judgeNext();
        return;
    }

    //
    // 再実行 前処理 ------------------------------------------------
    @Override
    public void sReStart(String btnText) {
        //if(btnText.length()!=0) this.actText = btnText;     //行動を保存
        switch (gcd.tmpLocal){
            case("win"):
                judgeNext();
                break;
            case("lose"):
                judgeNext();
                break;
            case("escape"):
                judgeNext();
                break;
        }
        return;
    }


    // 行動判定関数   ------------------------------------------------
    void judgeNext(){
        for(int i=0; i<eventList.size(); i++) {    //actionファイルを一行ずつチェックしていく
            String fileInfo[] = eventList.get(i).split(",");
            if(checkLocalState(fileInfo[0]) == false) continue;  //local判定
            if(checkBtnAction(fileInfo[1]) == false) continue;  //btn判定
            if(gcd.checkGlobalFlag(fileInfo[2]) == false) continue;    //Flag判定

            //処理
            mainImg = fileInfo[3];
            objectImg = fileInfo[4];
            personImg = fileInfo[5];
            chatImg = fileInfo[6];

            String[] bgmInfo = fileInfo[7].split("=");
            if(bgmInfo.length == 2) {
                bgm = bgmInfo[0];
                bgmRePos = gcd.str2int(bgmInfo[1]);
            }
            mainText = gcd.raw2message(fileInfo[8]);    // brを改行する

            btnTexts[0] = fileInfo[9];
            btnTexts[1] = fileInfo[10];
            btnTexts[2] = fileInfo[11];
            btnTexts[3] = fileInfo[12];
            btnTexts[4] = fileInfo[13];

            localState = fileInfo[14];

            gcd.updateGlobalFlag(fileInfo[15]);

            gcd.nextState = fileInfo[16];
            gcd.tmpLocal = fileInfo[17];
            gcd.extFileNames = fileInfo[18];

            return;
        }
    }

}
