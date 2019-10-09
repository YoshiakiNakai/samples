import java.util.ArrayList;

/**
 * Created by Nakai Yoshiaki on 2017/12/07.
 */

public class ISsetSkill extends GameState {

    Character chara;
    String menuState = "0";
    String menu = "0";
    String sort = "";

    int changeIdx = -1;
    int tmpIdx = -1;

    //初期化
    void init(CommonData cd, ArrayList<ArrayList<String>> extFileData){
        //this.eventList = extFileData.get(0);       //外部データの受け取り
        this.gcd = cd;                  //共通データの受け取り
        localState = gcd.tmpLocal;  // localStateの受け取り  // 開いたメニューの種類

        chara = gcd.charaMem.get(0);
        menuState = "0";
        menu = "0";
        sort = "";

        changeIdx = -1;
        tmpIdx = -1;
    };

    //コンストラクタ
    public ISsetSkill(){
    }

    String getStatusText() {
        String sts = "";
        sts += "戦闘スキル";
        for (int i = 0; i < chara.skills.size(); i++) {
            sts += "\n" + chara.skills.get(i).name;
        }

        if(menu.equals("戦闘スキル変更")){
            if(menuState.equals("系統選択") || menuState.equals("所持スキル表示") || menuState.equals("説明変更")){
                sts += "\n\n変更中スキル";
                sts += "\n" + chara.skills.get(changeIdx).name;
            }
        }
        return sts;
    }

    // 実行   ------------------------------------------------
    public void sExec(String btnText) {
        if (btnText.length() != 0) this.actText = btnText;     //行動を保存

        if (btnText.equals("閉じる")) {
            gcd.nextState = "pop";
            return;
        }

        // 開いたメニューの種類で分岐
        switch (menu) {
            case ("戦闘スキル変更"):
                changeSkill(btnText);
                break;
            case ("所持スキル確認"):
                belMenu(btnText);
                break;
            default:
                selectMenu(btnText);
                break;
        }
    }

    // 開くメニュー選択 -----------------------
    void selectMenu(String btnText) {
        mainText = "項目を選んでください。";
        btnTexts[0] = "戦闘スキル変更";
        btnTexts[1] = "所持スキル確認";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "閉じる";

        if (btnText.equals("閉じる")) {
            gcd.nextState = "pop";
            return;
        }

        // 開いたメニューの種類で分岐
        switch (btnText) {
            case ("戦闘スキル変更"):
                menu = "戦闘スキル変更";
                changeSkill(btnText);
                break;
            case ("所持スキル確認"):
                menu = "所持スキル確認";
                belMenu(btnText);
                break;
        }
    }

    //  =====================================================================
    void changeSkill(String btnText){
        //メニューの進行状態で分岐
        switch(menuState){
            case "0":
                showBtlsks();
                break;
            case("YesNo"):
                changeOr();
                break;
            case("系統選択"):
                showSortSkisAnd();
                break;
            case("所持スキル表示"):
                showBelSkis();
                break;
            case "説明変更":
                explainItemAnd();
                break;
        }

    }

    void showBtlsks(){
        mainText = "変更したいスキルを選択してください";
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "";
        // アイテムをボタンに表示
        for (int i = 0; i < chara.skills.size(); i++) {
            btnTexts[i] = chara.skills.get(i).name;
        }
        menuState = "YesNo";
    }

    void changeOr(){
        // スキル取得
        int btnIndex = -1;
        // インデックスの取得
        for(int i=chara.skills.size()-1; i>-1; i--) {    // 大きい数字から探す
            if( chara.skills.get(i).name.equals(actText)){
                btnIndex = i;
                break;
            }
        }
        changeIdx = btnIndex;  //変更するスキルの場所を記憶しておく
        chara.btlsk = gcd.setSkill( chara.skills.get(btnIndex).ID);  // スキルのセット
        mainText = chara.btlsk.explainEff;
        mainText += "\n" + chara.btlsk.explain;     //スキル説明
        btnTexts[0] = "変更する";
        btnTexts[1] = "戻る";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "変更を終える";

        menuState = "系統選択";
    }

    void showSortSkisAnd(){
        if(actText.equals("戻る")){ //戻るがタッチされていたら
            menuState = "YesNo";
            localState = "";
            sort = "";
            mainText = "変更したいスキルを選択してください";
            btnTexts[0] = "";
            btnTexts[1] = "";
            btnTexts[2] = "";
            btnTexts[3] = "";
            btnTexts[4] = "";
            // アイテムをボタンに表示
            for (int i = 0; i < chara.skills.size(); i++) {
                btnTexts[i] = chara.skills.get(i).name;
            }
            //gcd.nextState = "pop";
            return;
        }

        if(actText.equals("変更を終える")){ //戻るがタッチされていたら
            menu = "0";
            menuState = "0";
            localState = "";
            sort = "";
            selectMenu("");
            //gcd.nextState = "pop";
            return;
        }


        mainText = "装備したいスキルの系統を選択してください";
        btnTexts[0] = "物理系";
        btnTexts[1] = "貫通系";
        btnTexts[2] = "ガード系";
        btnTexts[3] = "特殊";
        btnTexts[4] = "やめる";

        menuState = "所持スキル表示";
    }

    void showBelSkis() {
        if (actText.equals("戻る")) { //戻るがタッチされていたら
            showSortSkisAnd();
            //gcd.nextState = "pop";
            return;
        }
        if(actText.equals("やめる")){ //戻るがタッチされていたら
            menu = "0";
            menuState = "0";
            localState = "";
            sort = "";
            selectMenu("");
            //gcd.nextState = "pop";
            return;
        }


        switch (actText) {
            case ("物理系"):
                menuState = "説明";
                sort = "物理系";
                gcd.bel = chara.learnedAt;
                break;
            case ("貫通系"):
                menuState = "説明";
                sort = "貫通系";
                gcd.bel = chara.learnedMg;
                break;
            case ("ガード系"):
                menuState = "説明";
                sort = "ガード系";
                gcd.bel = chara.learnedGd;
                break;
            case ("特殊"):
                menuState = "説明";
                sort = "特殊";
                gcd.bel = chara.learnedSp;
                break;
        }

        mainText = "装備したいスキルを選択してください";
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";

        // アイテムをボタンに表示
        for (int i = 0; i < gcd.bel.size(); i++) {
            btnTexts[i] = gcd.bel.get(i).name;
        }
        menuState = "説明変更";
    }



    void explainItemAnd() {
        if (actText.equals("戻る")) { //戻るがタッチされていたら
            mainText = "装備したいスキルの系統を選択してください";
            btnTexts[0] = "物理系";
            btnTexts[1] = "貫通系";
            btnTexts[2] = "ガード系";
            btnTexts[3] = "特殊";
            btnTexts[4] = "やめる";

            menuState = "所持スキル表示";
            return;
        }

        // スキル取得
        int btnIndex = -1;
        // インデックスの取得
        for(int i=gcd.bel.size()-1; i>-1; i--) {    // 大きい数字から探す
            if( gcd.bel.get(i).name.equals(actText)){
                btnIndex = i;
                break;
            }
        }
        chara.btlsk = gcd.setSkill( gcd.bel.get(btnIndex).ID);  // スキルのセット
        mainText = chara.btlsk.explainEff;
        mainText += "\n" + chara.btlsk.explain;     //スキル説明
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";
        // アイテムをボタンに表示
        for (int i = 0; i < gcd.bel.size(); i++) {
            btnTexts[i] = gcd.bel.get(i).name;
        }

        if(tmpIdx == btnIndex){
            chara.skills.set(changeIdx, chara.btlsk);   //スキル変更
            showBtlsks();
            menuState = "YesNo";
        }
        tmpIdx = btnIndex;

    }



    //  =====================================================================
    void belMenu(String btnText){
        //メニューの進行状態で分岐
        switch(menuState){
            case "0":
                showItem();   //選択項目をボタンに表示する
                break;
            case("系統選択"):
                showSortSkis(btnText);
                break;
            case "説明":
                explainItem(btnText);  // 説明文を表示する
                break;
        }
    }


    // 選択項目をボタンに表示する--------------------
    void showItem() {

        mainText = "確認したいスキルの系統を選んでください。";
        btnTexts[0] = "物理系";
        btnTexts[1] = "貫通系";
        btnTexts[2] = "ガード系";
        btnTexts[3] = "特殊";
        btnTexts[4] = "戻る";
        //次のstate
        menuState = "系統選択";
    }

    void showSortSkis(String btnText){
        if(btnText.equals("戻る")){ //戻るがタッチされていたら
            menu = "";
            menuState = "0";
            localState = "";
            sort = "";
            selectMenu("");
            //gcd.nextState = "pop";
            return;
        }

        mainText = "確認したいスキルを選択してください";
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";

        switch (btnText){
            case("物理系"):
                menuState = "説明";
                sort = "物理系";
                gcd.bel = chara.learnedAt;
                break;
            case("貫通系"):
                menuState = "説明";
                sort = "貫通系";
                gcd.bel = chara.learnedMg;
                break;
            case("ガード系"):
                menuState = "説明";
                sort = "ガード系";
                gcd.bel = chara.learnedGd;
                break;
            case("特殊"):
                menuState = "説明";
                sort = "特殊";
                gcd.bel = chara.learnedSp;
                break;
        }
        // アイテムをボタンに表示
        for (int i = 0; i < gcd.bel.size(); i++) {
            btnTexts[i] = gcd.bel.get(i).name;
        }


    }

    // タッチした項目の説明文を表示する--------------------
    void explainItem(String btnText){

        if(btnText.equals("戻る")){ //戻るがタッチされていたら
            menuState = "0";
            localState = "";
            sort = "";
            showItem();
            return;
        }

        // スキル取得
        int btnIndex = -1;
        // インデックスの取得
        for(int i=gcd.bel.size()-1; i>-1; i--) {    // 大きい数字から探す
            if( gcd.bel.get(i).name.equals(actText)){
                btnIndex = i;
                break;
            }
        }
        chara.btlsk = gcd.setSkill( gcd.bel.get(btnIndex).ID);  // スキルのセット
        mainText = chara.btlsk.explainEff;
        mainText += "\n" + chara.btlsk.explain;     //スキル説明
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";
        // アイテムをボタンに表示
        for (int i = 0; i < gcd.bel.size(); i++) {
            btnTexts[i] = gcd.bel.get(i).name;
        }
    }

}
