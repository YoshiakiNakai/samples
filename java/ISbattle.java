package amai.box_world1;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Nakai Yoshiaki on 2017/12/02.
 */

public class ISbattle extends GameState {

    Random rand;

    Character chara;
    Character enemy = new Character();

    // battle state内 status textフラグ
    String stsTextFlag = "";    // NoStsText
    //boolean enemyStsObs;        // 敵のステータス表示フラグ
    //boolean enemyActObs;       // 敵の次の行動表示フラグ

    int battleLoopDelayTime; //戦闘回しの時間間隔
    int tryEscape;  // 逃げる試行回数
    boolean escapeWarn = false; // 逃げる警告

    //初期化
    void init(CommonData cd, ArrayList<ArrayList<String>> extFileData) {
        //this.eventList = extFileData.get(0);       //外部データの受け取り
        this.gcd = cd;
        localState = "start";   // start, wait, do, win, lose, escape

        // 残りのメンバは startで初期化
    }

    // 処理する
    void sExec(String btnText) {
        if (btnText.length() != 0) this.actText = btnText;     //行動を保存
        battleProcess();    //戦闘処理
    }

    // ループ時の処理
    void sLoopExec(String actText) {
        battleProcess();    //戦闘処理
    }


    // 戦闘用ステータス欄表示 ----------------------------------------
    @Override
    String getStatusText() {
        String sts = "";
        if (stsTextFlag.equals("NoStsText")) return " ";     // エフェクト、stsの表示が消える

        Character chara = gcd.charaMem.get(0);
        chara.setTmpStatus("wantoKnow"); //スキル待機時効果の計算

        //sts += chara.name;
        sts += "HP " + chara.nHP + "/" + chara.maxHP;
        sts += "  速さ" + chara.tmpSp;
        // 状態異常表示
        if(chara.btlstates.size() > 0){
            sts += "\n";
            for(int i=0; i<chara.btlstates.size(); i++){
                sts += chara.btlstates.get(i).name + " ";
            }
        }
        /*
        //timeをバーで表示
        sts += "\n";
        for(int bar=0; bar<10; bar++){  // time barごとでひとつ
            if(bar*5-1 < chara.actTime && chara.actTime < bar*5+5){
                sts += "I"; // actTime
            }else if(chara.btlsk != null){
                if(bar*5-1 < chara.btlsk.needTime && chara.btlsk.needTime < bar*5+5) {
                    sts += "|"; // needTime
                }else{
                    sts += " ";
                }
            }else{
                sts += " ";
            }
        }*/
        sts += "\n行動 ";
        if(chara.btlsk != null) {
            sts += chara.btlsk.name;
            gcd.setSkillPower(chara);   // 威力の計算
            sts += "\n威力" + chara.btlsk.pownHP;
            sts += "  防御" + chara.tmpDef;
            // 攻撃中は残りウェイトを表示しない
            if(localState.equals("afterMyAtk") == false) {  // 技待機時
                sts += "\nウェイト ";
                int restTime = chara.btlsk.needTime - chara.actTime;
                if (restTime < 0) {
                    sts += "0";
                } else {
                    sts += restTime;
                }
            }else sts += "\n";//攻撃時
        }else{
            sts += "\n\n";//コマンド選択時
        }

        // 敵ステータス表示 -----------
        enemy.setTmpStatus("wantoKnow"); //スキル待機時効果の計算
        sts += "\n\n" + enemy.name;
        sts += "\nHP " + enemy.nHP;// + "/" + enemy.maxHP;
        sts += "     速さ" + enemy.tmpSp;

        // 状態異常表示
        if(enemy.btlstates.size() > 0){
            sts += "\n";
            for(int i=0; i<enemy.btlstates.size(); i++){
                sts += enemy.btlstates.get(i).name + " ";
            }
        }
        //timeをバーで表示
        /*
        sts += "\n";
        for(int bar=0; bar<10; bar++){  //
            if(bar*5-1 < enemy.actTime && enemy.actTime < bar*5+5){
                sts += "I"; // actTime
            }else if(enemy.btlsk != null){
                if(bar*5-1 < enemy.btlsk.needTime && enemy.btlsk.needTime < bar*5+5) {
                    sts += "|"; // needTime
                }else{
                    sts += " ";
                }
            }else{
                sts += " ";
            }
        }*/
        sts += "\n行動 ";
        if(enemy.btlsk != null) {
            sts += enemy.btlsk.name;
            gcd.setSkillPower(enemy);   // 固定威力を表示するために威力を計算する
            sts += "\n威力" + enemy.btlsk.pownHP;
            sts += "  防御" + enemy.tmpDef;
            sts += "  " + enemy.btlsk.explain;  // 貫通など特殊効果の記述
            // 攻撃中は残りウェイトを表示しない
            if (localState.equals("afterEAtk") == false) {
                sts += "\nウェイト ";
                int restTime = enemy.btlsk.needTime - enemy.actTime;
                if (restTime < 0) {
                    sts += "0";
                } else {
                    sts += restTime;
                }
            }else sts += "\n";
        }
        else{
            //sts += "\nウェイト余り " + enemy.actTime;
        }

        return sts;
    }

    //=========================戦闘処理========================================
    // 戦闘処理の分岐 ========================================
    void battleProcess() {
        loopDelayTime = battleLoopDelayTime;    //　startで値を設定
        switch (localState) { // start, wait, do, win, lose, escape
            case ("start"):  // バトル開始処理

                // 逃げる処理
                tryEscape = 0;
                escapeWarn = false;
                // キャラのセットと初期化処理
                this.chara = gcd.charaMem.get(0);   //参照渡し
                enemy = gcd.setEnemy(gcd.tmpLocal); // enemyID
                // action timeの設定
                rand = new Random();
                chara.actTime = 0;
                enemy.actTime = 0;
                chara.actionState = "commandWait";
                enemy.actionState = "commandWait";
                chara.btlsk = null;
                enemy.btlsk = null;
                chara.recoverAllAbno(); //状態異常回復
                chara.setTmpStatus("start");    // tmpStatusの初期化
                enemy.setTmpStatus("start");

                // UI処理
                this.canvasFlag = "object"; //canvas描画方法の変更
                mainImg = enemy.btlBgImg;   //"sreal0079"; //戦闘背景
                mainText = "";  // 表示戦闘文
                mainText = enemy.name + "が現れた。";
                personImg = enemy.img;   //敵画像
                objectImg = "";
                setBtn();                  //ボタンセット

                bgm = enemy.btlBgm;
                bgmRePos = enemy.bgmRePos;

                //システム処理
                localState = "actionSelect";         //次に行う処理
                //statusDispFlag = "battle";  // バトル用ステータス欄表示
                btnAbleFlag = "";        // ボタン設定
                battleLoopDelayTime = 500;    //  loopの速さ設定  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                break;

            case ("wait"):      // 待機
                localState = "do";
                btnAbleFlag = "wait";
                loopDelayTime = 500;
                break;
            case ("afterMyAtk"):      // 攻撃後の処理
                setBtn();
                personImg = enemy.img;   //敵画像
                stsTextFlag = "";       // ステータス欄の表示をもとに戻す
                mainText += "\n" + chara.btlsk.effDispText;    //表示文
                chara.btlsk = null;
                localState = "do";
                btnAbleFlag = "wait";
                checkBattleResult();        //勝敗チェック
                loopDelayTime = 500;
                break;
            case ("afterEAtk"):      // 敵の攻撃後の処理
                // getStatusText() で空文字を返し、ボタンを非表示にしていたのをもとに戻す。
                setBtn();
                personImg = enemy.img;   //敵画像
                stsTextFlag = "";       // ステータス欄の表示をもとに戻す
                mainText += "\n" + enemy.btlsk.effDispText;
                enemy.btlsk = null;
                localState = "do";
                btnAbleFlag = "wait";
                checkBattleResult();
                loopDelayTime = 500;
                break;
            case ("do"):      // バトル処理
                setBtn();
                battleAction();
                break;
            case("actionSelect"):   // コマンド入力
                judgeAction();
                break;
            case ("win"):      // 勝利
                btnTexts[0] = "次へ";
                btnTexts[1] = "";
                btnTexts[2] = "";
                btnTexts[3] = "";
                btnTexts[4] = "";
                personImg = "";
                //mainImg = "";
                mainText = enemy.name + "を倒した。";
                mainText += "\n結晶を " + enemy.experience + " 欠片手に入れた。";
                gcd.money += enemy.experience;  // 経験値加算
                gcd.charaMem.get(0).experience += enemy.experience; // ゲームバランス調整のために総取得 結晶数を数える
                //chara.experience = enemy.experience;    // 経験値をここで渡して、ISdungeonで加算処理を行う

                gcd.tmpLocal = localState;  // 戦闘結果を渡す
                localState = "end";         //  endに移る
                //gcd.nextState = "pop";      // 戦闘終了
                btnAbleFlag = "";           // ボタン設定も通常に戻す
                //statusDispFlag = "";        // ステータス表示を通常に戻す
                break;
            case ("lose"):      // 敗北
                gcd.nextState = "pop";
                gcd.tmpLocal = localState;
                //localState = "end";
                //statusDispFlag = "";
                //btnAbleFlag = "";
                chara.recoverAllAbno(); // 状態回復
                break;
            case ("escape"):      // 逃走
                gcd.nextState = "pop";
                gcd.tmpLocal = localState;
                //localState = "end";
                //statusDispFlag = "";
                //btnAbleFlag = "";
                chara.recoverAllAbno();
                break;
            case("end"):
                gcd.nextState = "pop";      // 戦闘終了
                chara.recoverAllAbno();
                break;

        }   // end switch
        return;
    }

    // バトル処理 =================================================
    void battleAction() {

        //if(checkBattleResult()) return;
        // プレイヤーの処理 ----------------------
        switch (chara.actionState) { //キャラの行動
            case ("commandWait"):    // コマンド入力待ちのとき
                if (chara.actTime >= 0) {
                    btnAbleFlag = "";   // ボタン入力可能にする  //のでloopDelayTime = 10はいらない
                    localState = "actionSelect";    // コマンド入力状態
                    chara.actionState = "select";   // 基本行動入力ボタン
                    return; // selectに移行したら、action timeをカウントせずに selectさせる
                }
                break;
            case ("actionWait"): //行動待ち
                if (chara.actTime >= chara.btlsk.needTime) {
                    // 処理
                    gcd.doSkill(chara, chara.sklobj);
                    chara.actCount++;   //行動回数カウント
                    // UI処理
                    selectMysklEffect(chara);   // スキル使用エフェクト
                    mainText = chara.btlsk.useDispText;
                    //システム処理
                    localState = "afterMyAtk";
                    //chara.btlsk = null;   //行動を削除
                    chara.actionState = "commandWait";   // 行動待ちにする
                    loopDelayTime = 500;

                    //アイテムの削除、または個数を減らす処理
                    if(chara.btlsk.sort.equals("アイテム")){
                        // インデックスの取得
                        int btnIndex = -1;
                        for (int i = chara.skills.size() - 1; i > -1; i--) {    // 大きい数字から探す
                            if (chara.skills.get(i).name.equals(chara.btlsk.name)) {
                                btnIndex = i;
                                chara.skills.remove(i);
                                break;
                            }
                        }
                    }

                    return; // action timeを増やさない    //技発動後、すでに time>=0ならcommand入力に移行する
                }
                break;
            case ("escapeWait"): //逃走
                if (chara.actTime >= chara.btlsk.needTime) {
                    // 処理
                    gcd.doSkill(chara, chara.sklobj);
                    // UI処理
                    selectMysklEffect(chara);   // スキル使用エフェクト
                    mainText = chara.btlsk.useDispText;
                    //システム処理
                    if(judgeEscape()) {// 逃走成功
                        localState = "escape";
                    }else { // 逃走失敗
                        localState = "afterMyAtk";
                    }
                    //chara.btlsk = null;   //行動を削除
                    chara.actionState = "commandWait";   // 行動待ちにする
                    loopDelayTime = 500;

                    return; // action timeを増やさない    //技発動後、すでに time>=0ならcommand入力に移行する
                }
                break;

        }

        // 敵の処理 --------------------------
        switch (enemy.actionState) { //敵の行動
            case ("commandWait"):    // コマンド入力待ちのとき
                if (enemy.actTime >= 0) {
                    enemyDecideAction();
                    enemy.actionState = "actionWait";   // btn選択処理にする
                    loopDelayTime = 10;     // 敵のコマンド入力には時間遅延を発生させないようにする
                    return; // selectに移行したら、action timeをカウントせずに selectさせる
                }
                break;
            case ("actionWait"): //行動待ち
                if (enemy.actTime >= enemy.btlsk.needTime) {
                    // 処理
                    gcd.doSkill(enemy, enemy.sklobj);
                    enemy.actCount++;   //行動回数をカウント
                    // UI処理
                    mainText = enemy.btlsk.useDispText;
                    selectEsklEffect(enemy);
                    //システム処理
                    localState = "afterEAtk";
                    //enemy.btlsk = null;
                    enemy.actionState = "commandWait";   // 行動選択待ちにする
                    loopDelayTime = 500;
                    return; // action timeを増やさない    //技発動後、すでに time>=0ならcommand入力に移行する
                }
                break;
        }
        String abnoMess = "";
        //ボタン選択が全て終わっていたら、action timeを増やす
        abnoMess += chara.setTmpStatus("TimeCount");    //<<<<<<<<<<<状態異常処理<<<<<<<<<<<<<<<
        abnoMess += enemy.setTmpStatus("TimeCount");
        mainText += abnoMess;
        chara.actTime += chara.tmpSp;
        enemy.actTime += enemy.tmpSp;
        return;
    }

    // 逃走成功判定----------------------------
    boolean judgeEscape(){
        return true;
    }


    // エフェクト選択
    void selectMysklEffect(Character chara){
        switch(chara.btlsk.category){
            case("回復"):
            case("バフ"):
            case("デバフ"):
            case("ガード"):
                break;
            case("ダメージ"):
            default:
                personImg = "";          // エフェクト //enemy画像を非表示にする
                break;
        }
    }

    // エフェクト選択
    void selectEsklEffect(Character enemy){
        switch(enemy.btlsk.category){
            case("回復"):
            case("バフ"):
            case("デバフ"):
            case("ガード"):
                break;
            case("ダメージ"):
            default:
                stsTextFlag = "NoStsText";
                btnTexts[0] = "";
                btnTexts[1] = "";
                btnTexts[2] = "";
                btnTexts[3] = "";
                btnTexts[4] = "";
                break;
        }
    }

    // ボタン処理 -----------------------------------------------
    void setBtn() {
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "";
        for(int i=0; i<chara.skills.size(); i++) {
            btnTexts[i] = chara.skills.get(i).name;
        }
    }

    // 行動判定関数   -------------------------------------------------------------------------
    void judgeAction() {
        int btnIndex = -1;  // 押したボタンのindex、スキルID取得に用いる
        //switch (chara.actionState) {    // commandWait, actionWait, select
        //case("select"):
        // インデックスの取得
        for (int i = chara.skills.size() - 1; i > -1; i--) {    // 大きい数字から探す
            if (chara.skills.get(i).name.equals(actText)) {
                btnIndex = i;
                break;
            }
        }
        //最初のタッチはスキルを登録し、説明を表示する-------
        if (chara.btlsk == null || chara.btlsk.ID.equals(chara.skills.get(btnIndex).ID) == false) {
            chara.btlsk = gcd.setSkill(chara.skills.get(btnIndex).ID);  // スキルのセット
            mainText = chara.btlsk.explainEff;
            mainText += "\n" + chara.btlsk.explain;     //スキル説明
            chara.setTmpStatus("wantoKnow"); //スキル待機時効果の計算
            gcd.setSkillPower(chara);   // 威力を計算する
            //mainImg = chara.btlsk.img;
            //setBtn();
        }
        //同じものが二度タッチされたなら実行する-------------
        else {
            // 消費リソースが足りるかチェック
            if (chara.checkSklRes() == false) {
                //chara.actionState = "selectSkill";
                mainText = chara.getShortageText();
                return;
            }
            switch(chara.btlsk.sort) {
                case ("アイテム"):// アイテム使用時に削除処理
                case ("技能"):
                    //処理
                    localState = "do";
                    //chara.btlsk = gcd.setSkill();   //登録済み
                    selectSklobj(chara);   //使用対象選択
                    chara.actionState = "actionWait";   // 行動待ちにする
                    btnAbleFlag = "wait";   // ボタン入力不可にする
                    mainText = chara.btlsk.waitDispText;   //待機メッセージ
                    mainText += gcd.setAbnoGetMess(chara.btlsk.waitAbno, chara);   // 状態付与と罹患メッセージ取得
                    break;
                case ("敗北宣言"):
                case ("逃走"):
                case ("逃げる"):
                    if (enemy.escapeDifficult < 0) {
                        //逃走不可
                        mainText = "逃げるわけにはいかない。";
                    } else {
                        // 逃げる
                        localState = "do";
                        chara.btlsk = gcd.setSkill(chara.escape.ID);   //行動を登録
                        chara.sklobj = chara;   // いらないんだけどね
                        chara.actionState = "escapeWait";   // 行動待ちにする
                        selectSklobj(chara);   //使用対象選択
                        btnAbleFlag = "wait";   // ボタン入力不可にする
                    }
                    break;
            }
        }
    }


    // スキル使用対象の選択
    void selectSklobj(Character chara){
        switch (chara.btlsk.category){
            case("ダメージ"):
            case("デバフ"):
                chara.sklobj = enemy;
                break;
            case("回復"):
            case("バフ"):
            case("ガード"):
                chara.sklobj = chara;
                break;
            default:
                chara.sklobj = chara;
                break;
        }

    }


    //======================================= 敵AI =================================================
    //ランダム技選択
    boolean eDecideRndAction(){

        int prioritySum = 0;    // rnd優先度合計値取得
        for(int i=0; i<enemy.btlskPriority.size(); i++){
            prioritySum += enemy.btlskPriority.get(i);
        }

        // rndイベント選択
        int r = rand.nextInt(prioritySum);
        int x=0;
        int btlskIndex = 0;
        for(int i=0; i<enemy.btlskPriority.size(); i++){
            x += enemy.btlskPriority.get(i);
            if(r < x){
                enemy.btlsk = gcd.setSkill(enemy.skills.get(i).ID);   //行動を登録
                btlskIndex = i;
                break;
            }
        }

        // 技を使えるか確認
        if(enemy.checkSklRes() == false){
            //足りなければ、次の技を使用する
            enemy.btlsk = gcd.setSkill(enemy.skills.get(btlskIndex+1).ID);   //行動を登録
            //優先度 1&0&1とかにしといて、最初の技が使えなくたったら、最初の技の代わりに優先度0の技を使うとかできる
        }
        return true;
    }
    //指定ターン技選択
    boolean eDecideTurnAction(){
        for(int i=0; i<enemy.turnSkills.size(); i++){
            if(enemy.btlskTurn.get(i) == enemy.actCount){
                enemy.btlsk = gcd.setSkill(enemy.turnSkills.get(i).ID);   //行動を登録
                return true;
            }
        }
        return false;
    }
    //指定ターン毎技選択
    boolean eDecidePerTurnAction(){
        for(int i=0; i<enemy.perTurnSkills.size(); i++){
            if(enemy.actCount % enemy.btlskPerTurn.get(i) == 0){
                enemy.btlsk = gcd.setSkill(enemy.perTurnSkills.get(i).ID);   //行動を登録
                return true;
            }
        }
        return false;
    }
    //相手依存技選択
    boolean eDecideDependAction(){
        for(int i=0; i<enemy.dependSkills.size(); i++) {    // ガード, サンダー
            if(checkBtlskDepCondAtk(enemy.btlskDependAtk.get(i)) == false) continue;    //>=20, なし
            if(checkBtlskDepCondDef(enemy.btlskDependDef.get(i)) == false) continue;    //
            if(checkBtlskDepCondWait(enemy.btlskDependWait.get(i)) == false) continue;    //
            if(checkBtlskDepCondSpe(enemy.btlskDependSpecial.get(i)) == false) continue;    //
            if(checkBtlskDepCondMy(enemy.btlskDependMy.get(i)) == false) continue;


            enemy.btlsk = gcd.setSkill(enemy.dependSkills.get(i).ID);   //行動を登録
            return true;
        }
        return false;
    }
    boolean checkBtlskDepCondAtk(String condstr){
        String[] cond = condstr.split("=");
        chara.setTmpStatus("wantoKnow");
        switch(cond[0]){
            case(">"):
                if(chara.btlsk == null) return false;
                gcd.setSkillPower(chara);   // 威力の計算
                if(chara.btlsk.pownHP > gcd.str2int(cond[1])) return true;
                break;
            case("<"):
                if(chara.btlsk == null) return true;
                gcd.setSkillPower(chara);   // 威力の計算
                if(chara.btlsk.pownHP < gcd.str2int(cond[1])) return true;
                break;
            case("なし"):
                return true;
        }
        return false;
    }
    boolean checkBtlskDepCondDef(String condstr){
        String[] cond = condstr.split("=");
        chara.setTmpStatus("wantoKnow");
        switch(cond[0]){
            case(">"):
                if(chara.tmpDef > gcd.str2int(cond[1])) return true;
                break;
            case("<"):
                if(chara.tmpDef < gcd.str2int(cond[1])) return true;
                break;
            case("なし"):
                return true;
        }
        return false;
    }
    boolean checkBtlskDepCondWait(String condstr){
        String[] cond = condstr.split("=");
        chara.setTmpStatus("wantoKnow");
        switch(cond[0]){
            case(">"):
                if(chara.btlsk != null) {
                    int loopNum = 0;
                    int restTime = chara.btlsk.needTime - chara.actTime;
                    while(restTime > 0){
                        restTime -= chara.tmpSp;
                        loopNum++;
                    }
                    if (loopNum > gcd.str2int(cond[1]))
                        return true;
                }else{
                    return true;
                }
                break;
            case("<"):
                if(chara.btlsk != null) {
                    int loopNum = 0;
                    int restTime = chara.btlsk.needTime - chara.actTime;
                    while (restTime > 0) {
                        restTime -= chara.tmpSp;
                        loopNum++;
                    }
                    if (loopNum < gcd.str2int(cond[1]))
                        return true;
                }else{
                    return false;
                }
                break;
            case("なし"):
                return true;
        }
        return false;
    }
    boolean checkBtlskDepCondSpe(String condstr){
        String[] cond = condstr.split("=");
        chara.setTmpStatus("wantoKnow");
        switch(cond[0]){
            case("resist"):
                if(cond.length == 1){
                    if(chara.btlsk.resistText.length() == 0) return true;
                    else return false;
                }
                if(cond[1].equals("tmpDef")){
                    if(chara.btlsk.resistText.equals("tmpDef")) return true;
                }
                break;
            case("なし"):
                return true;
        }
        return false;
    }
    boolean checkBtlskDepCondMy(String condstr){
        String[] conds = condstr.split("_");    // nHP=<=60_tmpSp=>=20
        for(int cn=0; cn<conds.length; cn++) {

            String[] cond = conds[cn].split("="); // nHP=<=60, tmpSp=>=20
            chara.setTmpStatus("wantoKnow");
            switch (cond[0]) {
                case ("maxHP"):
                    switch (cond[1]) {
                        case (">"):
                            if (enemy.maxHP > gcd.str2int(cond[2])) continue;
                            return false;
                        case ("<"):
                            if (enemy.maxHP < gcd.str2int(cond[2])) continue;
                            return false;
                        case ("なし"):
                            continue;
                    }
                    break;
                case ("nHP"):
                    switch (cond[1]) {
                        case (">"):
                            if (enemy.nHP > gcd.str2int(cond[2])) continue;
                            return false;
                        case ("<"):
                            if (enemy.nHP < gcd.str2int(cond[2])) continue;
                            return false;
                        case ("なし"):
                            continue;
                    }
                    break;
                case ("tmpSp"):
                    switch (cond[1]) {
                        case (">"):
                            if (enemy.tmpSp > gcd.str2int(cond[2])) continue;
                            return false;
                        case ("<"):
                            if (enemy.tmpSp < gcd.str2int(cond[2])) continue;
                            return false;
                        case ("なし"):
                            continue;
                    }
                    break;
                case ("なし"):
                    continue;
            }
        }
        return true;
    }


    // 行動判定関数   ------------------------------------------------
    void enemyDecideAction(){

        // 使用スキル選択
        if(eDecideTurnAction() == false){   //特定ターンスキル
            if(eDecidePerTurnAction() == false){    //特定ターン毎スキル
                if(eDecideDependAction() == false){     //相手の行動状態に依存したスキル
                    eDecideRndAction(); //ランダムスキル
                }
            }
        }

        enemySelectSklobj(enemy);        // 使用対象選択
        enemy.actionState = "actionWait";   // 行動待ちにする
        enemy.setTmpStatus("wantoKnow"); //スキル待機時効果の計算
        gcd.setSkillPower(enemy);   // 固定威力を表示するために威力を計算する
        mainText += enemy.btlsk.waitDispText;   // 待機メッセージ
        mainText += gcd.setAbnoGetMess(enemy.btlsk.waitAbno, enemy);   // 状態付与と罹患メッセージ取得
    }

    // スキル使用対象の選択
    void enemySelectSklobj(Character enemy){
        switch (enemy.btlsk.category){
            case("ダメージ"):
            case("デバフ"):
                enemy.sklobj = chara;
                break;
            case("回復"):
            case("バフ"):
            case("ガード"):
                enemy.sklobj = enemy;
                break;
            default:
                enemy.sklobj = enemy;
        }

    }


    // 戦闘結果判定、勝負がついていたらtrue
    boolean checkBattleResult(){
        if(chara.nHP < 1){
            localState = "lose";
            return true;
        }
        else if(enemy.nHP < 1){
            localState = "win";
            return true;
        }
        return false;
    }

}
