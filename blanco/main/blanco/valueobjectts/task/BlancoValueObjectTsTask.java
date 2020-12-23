package blanco.valueobjectts.task;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import blanco.valueobjectts.task.valueobject.BlancoValueObjectTsProcessInput;

/**
 * Apache Antタスク [BlancoValueObjectTs]のクラス。
 *
 * バリューオブジェクト定義書からソースコードを自動生成する BlancoValueObjectTsのAntTaskです。<br>
 * このクラスでは、Apache Antタスクで一般的に必要なチェックなどのコーディングを肩代わりします。
 * 実際の処理は パッケージ[blanco.valueobjectts.task]にBlancoValueObjectTsBatchProcessクラスを作成して記述してください。<br>
 * <br>
 * Antタスクへの組み込み例<br>
 * <pre>
 * &lt;taskdef name=&quot;blancovalueobjectts&quot; classname=&quot;blanco.valueobjectts.task.BlancoValueObjectTsTask">
 *     &lt;classpath&gt;
 *         &lt;fileset dir=&quot;lib&quot; includes=&quot;*.jar&quot; /&gt;
 *         &lt;fileset dir=&quot;lib.ant&quot; includes=&quot;*.jar&quot; /&gt;
 *     &lt;/classpath&gt;
 * &lt;/taskdef&gt;
 * </pre>
 */
public class BlancoValueObjectTsTask extends Task {
    /**
     * バリューオブジェクト定義書からソースコードを自動生成する BlancoValueObjectTsのAntTaskです。
     */
    protected BlancoValueObjectTsProcessInput fInput = new BlancoValueObjectTsProcessInput();

    /**
     * フィールド [metadir] に値がセットされたかどうか。
     */
    protected boolean fIsFieldMetadirProcessed = false;

    /**
     * フィールド [targetdir] に値がセットされたかどうか。
     */
    protected boolean fIsFieldTargetdirProcessed = false;

    /**
     * フィールド [tmpdir] に値がセットされたかどうか。
     */
    protected boolean fIsFieldTmpdirProcessed = false;

    /**
     * フィールド [encoding] に値がセットされたかどうか。
     */
    protected boolean fIsFieldEncodingProcessed = false;

    /**
     * フィールド [tabs] に値がセットされたかどうか。
     */
    protected boolean fIsFieldTabsProcessed = false;

    /**
     * フィールド [xmlrootelement] に値がセットされたかどうか。
     */
    protected boolean fIsFieldXmlrootelementProcessed = false;

    /**
     * フィールド [sheetType] に値がセットされたかどうか。
     */
    protected boolean fIsFieldSheetTypeProcessed = false;

    /**
     * フィールド [targetStyle] に値がセットされたかどうか。
     */
    protected boolean fIsFieldTargetStyleProcessed = false;

    /**
     * フィールド [listClass] に値がセットされたかどうか。
     */
    protected boolean fIsFieldListClassProcessed = false;

    /**
     * フィールド [lineSeparator] に値がセットされたかどうか。
     */
    protected boolean fIsFieldLineSeparatorProcessed = false;

    /**
     * フィールド [generateToJson] に値がセットされたかどうか。
     */
    protected boolean fIsFieldGenerateToJsonProcessed = false;

    /**
     * フィールド [listTmpdir] に値がセットされたかどうか。
     */
    protected boolean fIsFieldListTmpdirProcessed = false;

    /**
     * フィールド [searchTmpdir] に値がセットされたかどうか。
     */
    protected boolean fIsFieldSearchTmpdirProcessed = false;

    /**
     * verboseモードで動作させるかどうか。
     *
     * @param arg verboseモードで動作させるかどうか。
     */
    public void setVerbose(final boolean arg) {
        fInput.setVerbose(arg);
    }

    /**
     * verboseモードで動作させるかどうか。
     *
     * @return verboseモードで動作させるかどうか。
     */
    public boolean getVerbose() {
        return fInput.getVerbose();
    }

    /**
     * Antタスクの[metadir]アトリビュートのセッターメソッド。
     *
     * 項目番号: 1<br>
     * メタディレクトリ。xlsファイルの格納先または xmlファイルの格納先を指定します。<br>
     *
     * @param arg セットしたい値
     */
    public void setMetadir(final String arg) {
        fInput.setMetadir(arg);
        fIsFieldMetadirProcessed = true;
    }

    /**
     * Antタスクの[metadir]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 1<br>
     * メタディレクトリ。xlsファイルの格納先または xmlファイルの格納先を指定します。<br>
     * 必須アトリビュートです。Apache Antタスク上で必ず値が指定されます。<br>
     *
     * @return このフィールドの値
     */
    public String getMetadir() {
        return fInput.getMetadir();
    }

    /**
     * Antタスクの[targetdir]アトリビュートのセッターメソッド。
     *
     * 項目番号: 2<br>
     * 出力先フォルダを指定します。無指定の場合にはカレント直下のblancoを用います。<br>
     *
     * @param arg セットしたい値
     */
    public void setTargetdir(final String arg) {
        fInput.setTargetdir(arg);
        fIsFieldTargetdirProcessed = true;
    }

    /**
     * Antタスクの[targetdir]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 2<br>
     * 出力先フォルダを指定します。無指定の場合にはカレント直下のblancoを用います。<br>
     * デフォルト値[blanco]が設定されています。Apache Antタスク上でアトリビュートの指定が無い場合には、デフォルト値が設定されます。<br>
     *
     * @return このフィールドの値
     */
    public String getTargetdir() {
        return fInput.getTargetdir();
    }

    /**
     * Antタスクの[tmpdir]アトリビュートのセッターメソッド。
     *
     * 項目番号: 3<br>
     * テンポラリディレクトリを指定します。無指定の場合にはカレント直下のtmpを用います。<br>
     *
     * @param arg セットしたい値
     */
    public void setTmpdir(final String arg) {
        fInput.setTmpdir(arg);
        fIsFieldTmpdirProcessed = true;
    }

    /**
     * Antタスクの[tmpdir]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 3<br>
     * テンポラリディレクトリを指定します。無指定の場合にはカレント直下のtmpを用います。<br>
     * デフォルト値[tmp]が設定されています。Apache Antタスク上でアトリビュートの指定が無い場合には、デフォルト値が設定されます。<br>
     *
     * @return このフィールドの値
     */
    public String getTmpdir() {
        return fInput.getTmpdir();
    }

    /**
     * Antタスクの[encoding]アトリビュートのセッターメソッド。
     *
     * 項目番号: 4<br>
     * 自動生成するソースファイルの文字エンコーディングを指定します。<br>
     *
     * @param arg セットしたい値
     */
    public void setEncoding(final String arg) {
        fInput.setEncoding(arg);
        fIsFieldEncodingProcessed = true;
    }

    /**
     * Antタスクの[encoding]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 4<br>
     * 自動生成するソースファイルの文字エンコーディングを指定します。<br>
     *
     * @return このフィールドの値
     */
    public String getEncoding() {
        return fInput.getEncoding();
    }

    /**
     * Antタスクの[tabs]アトリビュートのセッターメソッド。
     *
     * 項目番号: 5<br>
     * タブをwhite spaceいくつで置き換えるか、という値です。<br>
     *
     * @param arg セットしたい値
     */
    public void setTabs(final String arg) {
        try {
            fInput.setTabs(Integer.parseInt(arg));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Antタスクの[tabs]アトリビュートに与えられた値の数値解析に失敗しました。" + e.toString());
        }
        fIsFieldTabsProcessed = true;
    }

    /**
     * Antタスクの[tabs]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 5<br>
     * タブをwhite spaceいくつで置き換えるか、という値です。<br>
     * デフォルト値[4]が設定されています。Apache Antタスク上でアトリビュートの指定が無い場合には、デフォルト値が設定されます。<br>
     *
     * @return このフィールドの値
     */
    public String getTabs() {
        return String.valueOf(fInput.getTabs());
    }

    /**
     * Antタスクの[xmlrootelement]アトリビュートのセッターメソッド。
     *
     * 項目番号: 6<br>
     * XML ルート要素のアノテーションを出力するかどうか。JDK 1.6 以降が必要。<br>
     *
     * @param arg セットしたい値
     */
    public void setXmlrootelement(final boolean arg) {
        fInput.setXmlrootelement(arg);
        fIsFieldXmlrootelementProcessed = true;
    }

    /**
     * Antタスクの[xmlrootelement]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 6<br>
     * XML ルート要素のアノテーションを出力するかどうか。JDK 1.6 以降が必要。<br>
     * デフォルト値[false]が設定されています。Apache Antタスク上でアトリビュートの指定が無い場合には、デフォルト値が設定されます。<br>
     *
     * @return このフィールドの値
     */
    public boolean getXmlrootelement() {
        return fInput.getXmlrootelement();
    }

    /**
     * Antタスクの[sheetType]アトリビュートのセッターメソッド。
     *
     * 項目番号: 7<br>
     * meta定義書が期待しているプログラミング言語を指定します<br>
     *
     * @param arg セットしたい値
     */
    public void setSheetType(final String arg) {
        fInput.setSheetType(arg);
        fIsFieldSheetTypeProcessed = true;
    }

    /**
     * Antタスクの[sheetType]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 7<br>
     * meta定義書が期待しているプログラミング言語を指定します<br>
     * デフォルト値[java]が設定されています。Apache Antタスク上でアトリビュートの指定が無い場合には、デフォルト値が設定されます。<br>
     *
     * @return このフィールドの値
     */
    public String getSheetType() {
        return fInput.getSheetType();
    }

    /**
     * Antタスクの[targetStyle]アトリビュートのセッターメソッド。
     *
     * 項目番号: 8<br>
     * 出力先フォルダの書式を指定します。&lt;br&gt;\nblanco: [targetdir]/main&lt;br&gt;\nmaven: [targetdir]/main/java&lt;br&gt;\nfree: [targetdir](targetdirが無指定の場合はblanco/main)<br>
     *
     * @param arg セットしたい値
     */
    public void setTargetStyle(final String arg) {
        fInput.setTargetStyle(arg);
        fIsFieldTargetStyleProcessed = true;
    }

    /**
     * Antタスクの[targetStyle]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 8<br>
     * 出力先フォルダの書式を指定します。&lt;br&gt;\nblanco: [targetdir]/main&lt;br&gt;\nmaven: [targetdir]/main/java&lt;br&gt;\nfree: [targetdir](targetdirが無指定の場合はblanco/main)<br>
     * デフォルト値[blanco]が設定されています。Apache Antタスク上でアトリビュートの指定が無い場合には、デフォルト値が設定されます。<br>
     *
     * @return このフィールドの値
     */
    public String getTargetStyle() {
        return fInput.getTargetStyle();
    }

    /**
     * Antタスクの[listClass]アトリビュートのセッターメソッド。
     *
     * 項目番号: 9<br>
     * 未指定または空文字でなかった場合に、対象ディレクトリ内の自動生成したクラスの一覧をプロパティにもつValueObjectを生成します。ValueObjectのクラス名を指定します。事前にExcelシートの作成が必要です。<br>
     *
     * @param arg セットしたい値
     */
    public void setListClass(final String arg) {
        fInput.setListClass(arg);
        fIsFieldListClassProcessed = true;
    }

    /**
     * Antタスクの[listClass]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 9<br>
     * 未指定または空文字でなかった場合に、対象ディレクトリ内の自動生成したクラスの一覧をプロパティにもつValueObjectを生成します。ValueObjectのクラス名を指定します。事前にExcelシートの作成が必要です。<br>
     *
     * @return このフィールドの値
     */
    public String getListClass() {
        return fInput.getListClass();
    }

    /**
     * Antタスクの[lineSeparator]アトリビュートのセッターメソッド。
     *
     * 項目番号: 10<br>
     * 行末記号をしていします。LF=0x0a, CR=0x0d, CFLF=0x0d0x0a とします。LFがデフォルトです。<br>
     *
     * @param arg セットしたい値
     */
    public void setLineSeparator(final String arg) {
        fInput.setLineSeparator(arg);
        fIsFieldLineSeparatorProcessed = true;
    }

    /**
     * Antタスクの[lineSeparator]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 10<br>
     * 行末記号をしていします。LF=0x0a, CR=0x0d, CFLF=0x0d0x0a とします。LFがデフォルトです。<br>
     * デフォルト値[LF]が設定されています。Apache Antタスク上でアトリビュートの指定が無い場合には、デフォルト値が設定されます。<br>
     *
     * @return このフィールドの値
     */
    public String getLineSeparator() {
        return fInput.getLineSeparator();
    }

    /**
     * Antタスクの[generateToJson]アトリビュートのセッターメソッド。
     *
     * 項目番号: 11<br>
     * toJSONメソッドを生成します<br>
     *
     * @param arg セットしたい値
     */
    public void setGenerateToJson(final boolean arg) {
        fInput.setGenerateToJson(arg);
        fIsFieldGenerateToJsonProcessed = true;
    }

    /**
     * Antタスクの[generateToJson]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 11<br>
     * toJSONメソッドを生成します<br>
     * デフォルト値[false]が設定されています。Apache Antタスク上でアトリビュートの指定が無い場合には、デフォルト値が設定されます。<br>
     *
     * @return このフィールドの値
     */
    public boolean getGenerateToJson() {
        return fInput.getGenerateToJson();
    }

    /**
     * Antタスクの[listTmpdir]アトリビュートのセッターメソッド。
     *
     * 項目番号: 12<br>
     * listClassの対象とするtmpディレクトリをカンマ区切りで指定します。指定ディレクトリ直下のvalueobjectディレクトリの下にxmlを探しにいきます。この回の生成で対象となるmetadirのクラスは全て対象となりますので、ここには指定しません。<br>
     *
     * @param arg セットしたい値
     */
    public void setListTmpdir(final String arg) {
        fInput.setListTmpdir(arg);
        fIsFieldListTmpdirProcessed = true;
    }

    /**
     * Antタスクの[listTmpdir]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 12<br>
     * listClassの対象とするtmpディレクトリをカンマ区切りで指定します。指定ディレクトリ直下のvalueobjectディレクトリの下にxmlを探しにいきます。この回の生成で対象となるmetadirのクラスは全て対象となりますので、ここには指定しません。<br>
     *
     * @return このフィールドの値
     */
    public String getListTmpdir() {
        return fInput.getListTmpdir();
    }

    /**
     * Antタスクの[searchTmpdir]アトリビュートのセッターメソッド。
     *
     * 項目番号: 13<br>
     * import 文自動生成時に package を検索にいくtmpdir をカンマ区切りで指定します。<br>
     *
     * @param arg セットしたい値
     */
    public void setSearchTmpdir(final String arg) {
        fInput.setSearchTmpdir(arg);
        fIsFieldSearchTmpdirProcessed = true;
    }

    /**
     * Antタスクの[searchTmpdir]アトリビュートのゲッターメソッド。
     *
     * 項目番号: 13<br>
     * import 文自動生成時に package を検索にいくtmpdir をカンマ区切りで指定します。<br>
     *
     * @return このフィールドの値
     */
    public String getSearchTmpdir() {
        return fInput.getSearchTmpdir();
    }

    /**
     * Antタスクのメイン処理。Apache Antから このメソッドが呼び出されます。
     *
     * @throws BuildException タスクとしての例外が発生した場合。
     */
    @Override
    public final void execute() throws BuildException {
        System.out.println("BlancoValueObjectTsTask begin.");

        // 項目番号[1]、アトリビュート[metadir]は必須入力です。入力チェックを行います。
        if (fIsFieldMetadirProcessed == false) {
            throw new BuildException("必須アトリビュート[metadir]が設定されていません。処理を中断します。");
        }

        if (getVerbose()) {
            System.out.println("- verbose:[true]");
            System.out.println("- metadir:[" + getMetadir() + "]");
            System.out.println("- targetdir:[" + getTargetdir() + "]");
            System.out.println("- tmpdir:[" + getTmpdir() + "]");
            System.out.println("- encoding:[" + getEncoding() + "]");
            System.out.println("- tabs:[" + getTabs() + "]");
            System.out.println("- xmlrootelement:[" + getXmlrootelement() + "]");
            System.out.println("- sheetType:[" + getSheetType() + "]");
            System.out.println("- targetStyle:[" + getTargetStyle() + "]");
            System.out.println("- listClass:[" + getListClass() + "]");
            System.out.println("- lineSeparator:[" + getLineSeparator() + "]");
            System.out.println("- generateToJson:[" + getGenerateToJson() + "]");
            System.out.println("- listTmpdir:[" + getListTmpdir() + "]");
            System.out.println("- searchTmpdir:[" + getSearchTmpdir() + "]");
        }

        try {
            // 実際のAntタスクの主処理を実行します。
            // この箇所でコンパイルエラーが発生する場合、BlancoValueObjectTsProcessインタフェースを実装して blanco.valueobjectts.taskパッケージに BlancoValueObjectTsProcessImplクラスを作成することにより解決できる場合があります。
            final BlancoValueObjectTsProcess proc = new BlancoValueObjectTsProcessImpl();
            if (proc.execute(fInput) != BlancoValueObjectTsBatchProcess.END_SUCCESS) {
                throw new BuildException("タスクは異常終了しました。");
            }
        } catch (IllegalArgumentException e) {
            if (getVerbose()) {
                e.printStackTrace();
            }
            throw new BuildException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException("タスクを処理中に例外が発生しました。処理を中断します。" + e.toString());
        } catch (Error e) {
            e.printStackTrace();
            throw new BuildException("タスクを処理中にエラーが発生しました。処理を中断します。" + e.toString());
        }
    }
}
