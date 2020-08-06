package blanco.valueobjectts.task;

import java.io.IOException;

import blanco.valueobjectts.task.valueobject.BlancoValueObjectTsProcessInput;

/**
 * バッチ処理クラス [BlancoValueObjectTsBatchProcess]。
 *
 * <P>バッチ処理の呼び出し例。</P>
 * <code>
 * java -classpath (クラスパス) blanco.valueobjectts.task.BlancoValueObjectTsBatchProcess -help
 * </code>
 */
public class BlancoValueObjectTsBatchProcess {
    /**
     * 正常終了。
     */
    public static final int END_SUCCESS = 0;

    /**
     * 入力異常終了。内部的にjava.lang.IllegalArgumentExceptionが発生した場合。
     */
    public static final int END_ILLEGAL_ARGUMENT_EXCEPTION = 7;

    /**
     * 入出力例外終了。内部的にjava.io.IOExceptionが発生した場合。
     */
    public static final int END_IO_EXCEPTION = 8;

    /**
     * 異常終了。バッチの処理開始に失敗した場合、および内部的にjava.lang.Errorまたはjava.lang.RuntimeExceptionが発生した場合。
     */
    public static final int END_ERROR = 9;

    /**
     * コマンドラインから実行された際のエントリポイントです。
     *
     * @param args コンソールから引き継がれた引数。
     */
    public static final void main(final String[] args) {
        final BlancoValueObjectTsBatchProcess batchProcess = new BlancoValueObjectTsBatchProcess();

        // バッチ処理の引数。
        final BlancoValueObjectTsProcessInput input = new BlancoValueObjectTsProcessInput();

        boolean isNeedUsage = false;
        boolean isFieldMetadirProcessed = false;

        // コマンドライン引数の解析をおこないます。
        for (int index = 0; index < args.length; index++) {
            String arg = args[index];
            if (arg.startsWith("-verbose=")) {
                input.setVerbose(Boolean.valueOf(arg.substring(9)).booleanValue());
            } else if (arg.startsWith("-metadir=")) {
                input.setMetadir(arg.substring(9));
                isFieldMetadirProcessed = true;
            } else if (arg.startsWith("-targetdir=")) {
                input.setTargetdir(arg.substring(11));
            } else if (arg.startsWith("-tmpdir=")) {
                input.setTmpdir(arg.substring(8));
            } else if (arg.startsWith("-encoding=")) {
                input.setEncoding(arg.substring(10));
            } else if (arg.startsWith("-tabs=")) {
                try {
                    input.setTabs(Integer.parseInt(arg.substring(6)));
                } catch (NumberFormatException e) {
                    System.out.println("BlancoValueObjectTsBatchProcess: 処理開始失敗。入力パラメータ[input]のフィールド[tabs]を数値(int)としてパースを試みましたが失敗しました。: " + e.toString());
                    System.exit(END_ILLEGAL_ARGUMENT_EXCEPTION);
                }
            } else if (arg.startsWith("-xmlrootelement=")) {
                input.setXmlrootelement(Boolean.valueOf(arg.substring(16)).booleanValue());
            } else if (arg.startsWith("-sheetType=")) {
                input.setSheetType(arg.substring(11));
            } else if (arg.startsWith("-targetStyle=")) {
                input.setTargetStyle(arg.substring(13));
            } else if (arg.startsWith("-listClass=")) {
                input.setListClass(arg.substring(11));
            } else if (arg.startsWith("-lineSeparator=")) {
                input.setLineSeparator(arg.substring(15));
            } else if (arg.startsWith("-generateToJson=")) {
                input.setGenerateToJson(Boolean.valueOf(arg.substring(16)).booleanValue());
            } else if (arg.startsWith("-searchTmpdir=")) {
                input.setSearchTmpdir(arg.substring(14));
            } else if (arg.equals("-?") || arg.equals("-help")) {
                usage();
                System.exit(END_SUCCESS);
            } else {
                System.out.println("BlancoValueObjectTsBatchProcess: 入力パラメータ[" + arg + "]は無視されました。");
                isNeedUsage = true;
            }
        }

        if (isNeedUsage) {
            usage();
        }

        if( isFieldMetadirProcessed == false) {
            System.out.println("BlancoValueObjectTsBatchProcess: 処理開始失敗。入力パラメータ[input]の必須フィールド値[metadir]に値が設定されていません。");
            System.exit(END_ILLEGAL_ARGUMENT_EXCEPTION);
        }

        int retCode = batchProcess.execute(input);

        // 終了コードを戻します。
        // ※注意：System.exit()を呼び出している点に注意してください。
        System.exit(retCode);
    }

    /**
     * 具体的なバッチ処理内容を記述するためのメソッドです。
     *
     * このメソッドに実際の処理内容を記述します。
     *
     * @param input バッチ処理の入力パラメータ。
     * @return バッチ処理の終了コード。END_SUCCESS, END_ILLEGAL_ARGUMENT_EXCEPTION, END_IO_EXCEPTION, END_ERROR のいずれかの値を戻します。
     * @throws IOException 入出力例外が発生した場合。
     * @throws IllegalArgumentException 入力値に不正が見つかった場合。
     */
    public int process(final BlancoValueObjectTsProcessInput input) throws IOException, IllegalArgumentException {
        // 入力パラメータをチェックします。
        validateInput(input);

        // この箇所でコンパイルエラーが発生する場合、BlancoValueObjectTsProcessインタフェースを実装して blanco.valueobjectts.taskパッケージに BlancoValueObjectTsProcessImplクラスを作成することにより解決できる場合があります。
        final BlancoValueObjectTsProcess process = new BlancoValueObjectTsProcessImpl();

        // 処理の本体を実行します。
        final int retCode = process.execute(input);

        return retCode;
    }

    /**
     * クラスをインスタンス化してバッチを実行する際のエントリポイントです。
     *
     * このメソッドは下記の仕様を提供します。
     * <ul>
     * <li>メソッドの入力パラメータの内容チェック。
     * <li>IllegalArgumentException, RuntimeException, Errorなどの例外をcatchして戻り値へと変換。
     * </ul>
     *
     * @param input バッチ処理の入力パラメータ。
     * @return バッチ処理の終了コード。END_SUCCESS, END_ILLEGAL_ARGUMENT_EXCEPTION, END_IO_EXCEPTION, END_ERROR のいずれかの値を戻します。
     * @throws IllegalArgumentException 入力値に不正が見つかった場合。
     */
    public final int execute(final BlancoValueObjectTsProcessInput input) throws IllegalArgumentException {
        try {
            // バッチ処理の本体を実行します。
            int retCode = process(input);

            return retCode;
        } catch (IllegalArgumentException ex) {
            System.out.println("BlancoValueObjectTsBatchProcess: 入力例外が発生しました。バッチ処理を中断します。:" + ex.toString());
            // 入力異常終了。
            return END_ILLEGAL_ARGUMENT_EXCEPTION;
        } catch (IOException ex) {
            System.out.println("BlancoValueObjectTsBatchProcess: 入出力例外が発生しました。バッチ処理を中断します。:" + ex.toString());
            // 入力異常終了。
            return END_IO_EXCEPTION;
        } catch (RuntimeException ex) {
            System.out.println("BlancoValueObjectTsBatchProcess: ランタイム例外が発生しました。バッチ処理を中断します。:" + ex.toString());
            ex.printStackTrace();
            // 異常終了。
            return END_ERROR;
        } catch (Error er) {
            System.out.println("BlancoValueObjectTsBatchProcess: ランタイムエラーが発生しました。バッチ処理を中断します。:" + er.toString());
            er.printStackTrace();
            // 異常終了。
            return END_ERROR;
        }
    }

    /**
     * このバッチ処理クラスの使い方の説明を標準出力に示すためのメソッドです。
     */
    public static final void usage() {
        System.out.println("BlancoValueObjectTsBatchProcess: Usage:");
        System.out.println("  java blanco.valueobjectts.task.BlancoValueObjectTsBatchProcess -verbose=値1 -metadir=値2 -targetdir=値3 -tmpdir=値4 -encoding=値5 -tabs=値6 -xmlrootelement=値7 -sheetType=値8 -targetStyle=値9 -listClass=値10 -lineSeparator=値11 -generateToJson=値12 -searchTmpdir=値13");
        System.out.println("    -verbose");
        System.out.println("      説明[verboseモードで動作させるかどうか。]");
        System.out.println("      型[真偽]");
        System.out.println("      デフォルト値[false]");
        System.out.println("    -metadir");
        System.out.println("      説明[メタディレクトリ。xlsファイルの格納先または xmlファイルの格納先を指定します。]");
        System.out.println("      型[文字列]");
        System.out.println("      必須パラメータ");
        System.out.println("    -targetdir");
        System.out.println("      説明[出力先フォルダを指定します。無指定の場合にはカレント直下のblancoを用います。]");
        System.out.println("      型[文字列]");
        System.out.println("      デフォルト値[blanco]");
        System.out.println("    -tmpdir");
        System.out.println("      説明[テンポラリディレクトリを指定します。無指定の場合にはカレント直下のtmpを用います。]");
        System.out.println("      型[文字列]");
        System.out.println("      デフォルト値[tmp]");
        System.out.println("    -encoding");
        System.out.println("      説明[自動生成するソースファイルの文字エンコーディングを指定します。]");
        System.out.println("      型[文字列]");
        System.out.println("    -tabs");
        System.out.println("      説明[タブをwhite spaceいくつで置き換えるか、という値です。]");
        System.out.println("      型[数値(int)]");
        System.out.println("      デフォルト値[4]");
        System.out.println("    -xmlrootelement");
        System.out.println("      説明[XML ルート要素のアノテーションを出力するかどうか。JDK 1.6 以降が必要。]");
        System.out.println("      型[真偽]");
        System.out.println("      デフォルト値[false]");
        System.out.println("    -sheetType");
        System.out.println("      説明[meta定義書が期待しているプログラミング言語を指定します]");
        System.out.println("      型[文字列]");
        System.out.println("      デフォルト値[java]");
        System.out.println("    -targetStyle");
        System.out.println("      説明[出力先フォルダの書式を指定します。<br>\nblanco: [targetdir]/main<br>\nmaven: [targetdir]/main/java<br>\nfree: [targetdir](targetdirが無指定の場合はblanco/main)]");
        System.out.println("      型[文字列]");
        System.out.println("      デフォルト値[blanco]");
        System.out.println("    -listClass");
        System.out.println("      説明[未指定または空文字でなかった場合に、対象ディレクトリ内の自動生成したクラスの一覧をプロパティにもつValueObjectを生成します。ValueObjectのクラス名を指定します。事前にExcelシートの作成が必要です。]");
        System.out.println("      型[文字列]");
        System.out.println("    -lineSeparator");
        System.out.println("      説明[行末記号をしていします。LF=0x0a, CR=0x0d, CFLF=0x0d0x0a とします。LFがデフォルトです。]");
        System.out.println("      型[文字列]");
        System.out.println("      デフォルト値[LF]");
        System.out.println("    -generateToJson");
        System.out.println("      説明[toJSONメソッドを生成します]");
        System.out.println("      型[真偽]");
        System.out.println("      デフォルト値[false]");
        System.out.println("    -searchTmpdir");
        System.out.println("      説明[listClassの対象とするtmpディレクトリをカンマ区切りで指定します。指定ディレクトリ直下のvalueobjectディレクトリの下にxmlを探しにいきます。]");
        System.out.println("      型[文字列]");
        System.out.println("    -? , -help");
        System.out.println("      説明[使い方を表示します。]");
    }

    /**
     * このバッチ処理クラスの入力パラメータの妥当性チェックを実施するためのメソッドです。
     *
     * @param input バッチ処理の入力パラメータ。
     * @throws IllegalArgumentException 入力値に不正が見つかった場合。
     */
    public void validateInput(final BlancoValueObjectTsProcessInput input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("BlancoBatchProcessBatchProcess: 処理開始失敗。入力パラメータ[input]にnullが与えられました。");
        }
        if (input.getMetadir() == null) {
            throw new IllegalArgumentException("BlancoValueObjectTsBatchProcess: 処理開始失敗。入力パラメータ[input]の必須フィールド値[metadir]に値が設定されていません。");
        }
    }
}
