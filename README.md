# kirikiri_editor

Kirikiri形式（`.ks`）のシナリオを、会話ブロック単位で編集・出力するデスクトップエディタです。  
Compose Multiplatform (JVM) で動作します。

## 主な機能
- 会話 (`TalkBlock`) の追加・編集
- 話者切替（F1〜F5）
- 表情変更
  - キャラクターグループ単位の表情変更
  - インライン `CharaFace` ブロックの追加・編集
- キャライベント (`CharaEvent`) / コマンド (`CommandBlock`) の表示
- Undo / Redo
- `.ks` ファイル出力（BOM付き UTF-8）

## 動作環境
- JDK 17+ 推奨
- Windows / macOS / Linux（JVM 実行環境）

## 起動方法
### Windows
```powershell
.\gradlew.bat :composeApp:run
```

### macOS / Linux
```bash
./gradlew :composeApp:run
```

## 操作方法（基本）
- `F1`〜`F5`: 話者選択
- `Enter`: 行追加（`[r]`）
- `Shift + Enter`: 改行タグ付き追加（`[l][r]`）
- `Ctrl + Enter`: ページ送りタグ付き追加（`[p]`）
- `Ctrl + Shift + Enter`: ボックス分割タグ付き追加（`[l][cm]`）
- `F1`〜`F5` を押しながら `Enter`: `CharaFace` 追加

## 出力
- 画面右下の `保存` ボタンで `./scenarios` に出力されます。
- 同名ファイルがある場合は連番（`*_0.ks`, `*_1.ks`, ...）で保存されます。

## 開発者向けドキュメント
内部構造・クラス図・関数仕様は以下を参照してください。

- [README.dev.md](README.dev.md)
