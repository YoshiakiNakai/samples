import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Nakai Yoshiaki on 2017/12/07.
 */

public class ISskiShop extends GameState {

    ArrayList<String> sltFile = new ArrayList<>();  //品揃え
    ArrayList<String> sltItem = new ArrayList<>();

    String page = "0";
    String skiCate = "";
    Character chara;
    Skill tmpSki;
    int tmpIdx = -1;


    //初期化
    void init(CommonData cd, ArrayList<ArrayList<String>> extFileData) {
        this.sltFile = extFileData.get(0);       //外部データの受け取り
        this.gcd = cd;                  //共通データの受け取り
        localState = gcd.tmpLocal;  // localStateの受け取り

        skiCate = "";
        page = "0";
        tmpSki = null;
        chara = gcd.charaMem.get(0);
        sltItem.clear();
        tmpIdx = -1;
    }

    // 処理する
    void sExec(String btnText) {
        if (btnText.length() != 0) this.actText = btnText;     //行動を保存

        switch (page) {
            case("0"):
                showSkiCate();
                break;
            case("選択"):
                showSelection();
                break;
            case("説明"):
                explainSki();
                break;
        }
    }

    // 系統表示
    void showSkiCate() {
        mainText = "いらっしゃい\n購入したいスキルの系統を選んでくれ。";
        btnTexts[0] = "物理系";
        btnTexts[1] = "貫通系";
        btnTexts[2] = "ガード系";
        btnTexts[3] = "特殊";
        btnTexts[4] = "戻る";

        page = "選択";
    }

    // 系統選択処理、系統ごとスキル表示
    void showSelection() {
        if (actText.equals("戻る")) {
            gcd.nextState = "pop";
            return;
        }
        skiCate = actText;

        mainText = "購入したいスキルを選んでくれ。";
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";

        parseSelectionFile(skiCate);    //ファイルからスキルIDを取ってくる
        // ボタンにスキルIDを表示
        for (int i = 0; i < sltItem.size(); i++) {
            btnTexts[i] = sltItem.get(i);
        }
        page = "説明";
    }

    // スキル説明。遷移なし、購入なら購入
    void explainSki(){
        if (actText.equals("戻る")) {
            showSkiCate();
            return;
        }

        if(actText.equals("購入")){
            purchase();
            return;
        }

        mainText = "";
        btnTexts[0] = "";
        btnTexts[1] = "";
        btnTexts[2] = "";
        btnTexts[3] = "";
        btnTexts[4] = "戻る";

        parseSelectionFile(skiCate);    //ファイルからスキルIDを取ってくる
        // ボタンにスキルIDを表示
        for (int i = 0; i < sltItem.size(); i++) {
            btnTexts[i] = sltItem.get(i);
        }

        // タッチしたスキル取得
        int btnIndex = -1;
        // インデックスの取得
        for(int i=sltItem.size()-1; i>-1; i--) {    // 大きい数字から探す
            if( sltItem.get(i).equals(actText)){
                btnIndex = i;
                tmpIdx = btnIndex;
                break;
            }
        }
        tmpSki = gcd.setSkill( sltItem.get(btnIndex));
        mainText = tmpSki.name;
        mainText += "\n" + tmpSki.explainEff;
        mainText += "\n" + tmpSki.explain;
        mainText += "\n価格: " + tmpSki.price + "欠片";
        btnTexts[btnIndex] = "購入";
    }

    void purchase(){

        if(gcd.money < tmpSki.price){
            mainText = "お金が足りない";
            return;
        }
        // お金を減らす
        gcd.money -= tmpSki.price;
        // 習得する
        switch (skiCate) {  // 習得済みスキルの除外
            case ("物理系"):
                chara.learnedAt.add(tmpSki);
                break;
            case ("貫通系"):
                chara.learnedMg.add(tmpSki);
                break;
            case ("ガード系"):
                chara.learnedGd.add(tmpSki);
                break;
            case ("特殊"):
                chara.learnedSp.add(tmpSki);
                break;
        }
        // 商品を削除する
        sltItem.remove(tmpIdx);

        //
        showSkiCate();
        mainText = "まいど";
    }

    // ファイルからスキルを取ってくる
    void parseSelectionFile(String skillCategory) {
        sltItem.clear();    //初期化
        for (int i = 0; i < sltFile.size(); i++) {
            String fileInfo[] = sltFile.get(i).split(",");
            if (skillCategory.equals(fileInfo[1]) == false) continue;
            sltItem.add(fileInfo[0]);   // スキルID追加
        }

        switch (skiCate) {  // 習得済みスキルの除外
            case ("物理系"):
                for(int i=chara.learnedAt.size()-1; i>-1; i--) {    // 大きい数字から探す
                    for(int j=sltItem.size()-1; j>-1; j--){
                        if(chara.learnedAt.get(i).ID.equals(sltItem.get(j))){
                            sltItem.remove(j);  // 習得済みスキルは除外
                        }
                    }
                }
                break;
            case ("貫通系"):
                for(int i=chara.learnedMg.size()-1; i>-1; i--) {    // 大きい数字から探す
                    for(int j=sltItem.size()-1; j>-1; j--){
                        if(chara.learnedMg.get(i).ID.equals(sltItem.get(j))){
                            sltItem.remove(j);  // 習得済みスキルは除外
                        }
                    }
                }
                break;
            case ("ガード系"):
                for(int i=chara.learnedGd.size()-1; i>-1; i--) {    // 大きい数字から探す
                    for(int j=sltItem.size()-1; j>-1; j--){
                        if(chara.learnedGd.get(i).ID.equals(sltItem.get(j))){
                            sltItem.remove(j);  // 習得済みスキルは除外
                        }
                    }
                }
                break;
            case ("特殊"):
                for(int i=chara.learnedSp.size()-1; i>-1; i--) {    // 大きい数字から探す
                    for(int j=sltItem.size()-1; j>-1; j--){
                        if(chara.learnedSp.get(i).ID.equals(sltItem.get(j))){
                            sltItem.remove(j);  // 習得済みスキルは除外
                        }
                    }
                }
                break;
        }
    }

}
