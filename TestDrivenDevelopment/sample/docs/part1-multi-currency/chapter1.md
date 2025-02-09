# 第１章 仮実装

## テスト駆動開発 はじめの一歩

### スタンス

- テスト駆動開発では、基本的に「どんなテストが通ったときに、目的とするコードが完成したと言えるのか？」と考える
- つまり、テストを先に考えるということ
- 目の前の仕事が複雑なときは、サイクルを細かくする
- そして、テスト駆動開発では、後述する小さなサイクルを回し続けることが大切

### ToDo リストを活用する

- 「こんなテストをしないといけないなぁ。」と思いついたら、それを ToDo リストに残していくようにする
- ToDo リストの中の、解決できそうなものから取り組んでいく

### テスト駆動開発における仮実装の小さなサイクル

1. 小さいテストを１つ書く
2. 全てのテストを実行し、１つ失敗することを確認する
3. 失敗を解消するための小さい変更を行う
4. 再テストを行い、すべて成功することを確認する
5. リファクタリングを行い、重複を除去する

### ToDo リスト

- 現在の ToDo リストは以下の通り
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [ ] amount を private にする
  - [ ] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？

### サンプルコード

- 現段階でのコードは以下の通り

```java
package money;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {
    @Test
    public void testMultiplication() {
        Dollar five = new Dollar(5);
        five.times(2);
        assertEquals(10, five.amount);
    }
}
```

```java
package money;

public class Dollar {
    int amount;

    Dollar(int amount) {
        this.amount = amount;
    }

    void times(int multiplier) {
        amount *= multiplier;
    }
}
```

# 第 2 章 明白な実装

## 正しい実装が分かるなら、明白な実装を行う

### 仮実装と明白な実装の違い

- 仮実装とは、キレイな実装方法がわからない場合に、とにかくテストが通るように、とりあえず書き、それからリファクタリングをしていくことで形を整えていく方法
- 明白な実装とは、綺麗な実装方法がアイデアとして、既に頭のある場合に、それをそのまま形にする方法

### 今回の課題

- 以下のコードから分かる通り、Dollar five の状態が times() によって変更されてしまっている。
- これは副作用と言われる望ましくない結果である
- できれば、オブジェクトの状態を変えるのではなく、欲しい結果は戻り値として受け取れるようにしたい。

```java
package money;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MoneyTest {
    @Test
    public void testMultiplication() {
        Dollar five = new Dollar(5);
        five.times(2);
        assertEquals(10, five.amount);
    }
}
```

### 課題に対して明白な実装を行う

1. 望ましい状態を期待したものに、テストコードを改変する
2. まずは、return null; のような空実装でよいので、times() を変更し、コンパイルを通す。
3. 正しいと思える実装を適用（明白な実装）し、テストを通す。

### 上記を行った結果

- 現在のコード

  ```java
  package money;

  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.*;

  public class MoneyTest {
      @Test
      public void testMultiplication() {
          Dollar five = new Dollar(5);
          Dollar product = five.times(2);
          assertEquals(10, product.amount);
          product = five.times(3);
          assertEquals(15, product.amount);
      }
  }
  ```

  ```java
  package money;

  public class Dollar {
      int amount;

      Dollar(int amount) {
          this.amount = amount;
      }

      Dollar times(int multiplier) {
          return new Dollar(amount * multiplier);
      }
  }
  ```

- 現在の ToDo リスト
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [ ] amount を private にする
  - [x] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？

# 第 3 章 三角測量

## 三角測量の手順とその必要性

### 今回の問題点

- Money は値オブジェクトである。
- 現実世界の通貨と同じく、製造番号等が違えど、5 ドルは他の 5 ドルと等しいと判断されなければならない
- しかし、現状では、five.equals(new Dollar(5)) は false となってしまう
- これが、true になるようする

### 三角測量とは

- テスト駆動開発における小さなサイクルの回し方の３つ目として三角測量という方法がある
- 仮実装は正しい実装のアイデアがない場合に、とりあえずテストが通るように書き、リファクタリングしていく手法
- 明白な実装は正しい実装のアイデアがある場合に、それをそのまま形にする手法
- 三角測量とは以下のような手法である
  - 正しい実装のアイデアがない場合に採用する
  - まずは、仮実装と同じく、テストケースを１つ用意し、それを通すように実装する
  - 次に、テストケースを追加する。実装を追加したテストケースにも通るよう改変していく
  - 必要ならば、テストケースの実装と実装の改変を繰り返していく。
  - このサイクルを経て、実装が一般化されていく
- あまり、一般的に用いられる手法ではないが、どうしても実装のアイデアが浮かばない場合に、違う角度から物事を考えるよいきっかけけになる

### 三角測量の手順

1. 以下のテストメソッドを MoneyTest に追加する。現状これは失敗する、

   ```java
       @Test
       public void testEquality() {
           assertTrue(new Dollar(5).equals(new Dollar(5)));
       }
   ```

2. 以下のように、Dollar クラスに equals() メソッドを追加する。これは空の実装でよい。

   ```java
       @Override
       public boolean equals(Object object) {
           return true;
       }
   ```

3. 以下のように、２つ目のテストケースを追加する。

   ```java
       @Test
       public void testEquality() {
           assertTrue(new Dollar(5).equals(new Dollar(5)));
           assertFalse(new Dollar(5).equals(new Dollar(6)));
       }
   ```

4. 以下のように、Dollar クラスに equals() メソッドを改変する。これにより、テストケースが通り、equals() メソッドが正しく動作するようになる。

   ```java
       @Override
       public boolean equals(Object object) {
           Dollar dollar = (Dollar) object;
           return amount == dollar.amount;
       }
   ```

### 結果

- 現在のコード

  ```java
  package money;

  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.*;

  public class MoneyTest {
      @Test
      public void testMultiplication() {
          Dollar five = new Dollar(5);
          Dollar product = five.times(2);
          assertEquals(10, product.amount);
          product = five.times(3);
          assertEquals(15, product.amount);
      }

      @Test
      public void testEquality() {
          assertTrue(new Dollar(5).equals(new Dollar(5)));
          assertFalse(new Dollar(5).equals(new Dollar(6)));
      }
  }
  ```

  ```java
  package money;

  public class Dollar {
      int amount;

      Dollar(int amount) {
          this.amount = amount;
      }

      Dollar times(int multiplier) {
          return new Dollar(amount * multiplier);
      }

      @Override
      public boolean equals(Object object) {
          Dollar dollar = (Dollar) object;
          return amount == dollar.amount;
      }
  }
  ```

- 現在の ToDo リスト
  - null との比較や、他クラスとのオブジェクトとの比較等も求められがちだが、現状不要なので、ToDo に追加しておくのみとする
    - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
    - [x] 5USD \* 2 = 10USD
    - [ ] amount を private にする
    - [x] Dollar の副作用どうする？
    - [ ] Money の丸め処理どうする？
    - [x] equals() メソッドを実装する
    - [ ] hashCode() メソッドを実装する
    - [ ] null との等価性比較
    - [ ] 他クラスとのオブジェクトとの等価性比較

# 第 4 章 意図を語るテスト

## テストコードを通していかにメソッドの意図を伝えるか

### 今回の目的

- 現状のテストコードからは、dollar.times() は Dollar を返すという意図が読み取れない
- テストコードからその意図が読み取れるようにする

### テストコードの改変

1. 10 や 15 といった数値では、Dollar のオブジェクトを返すという意図が読み取れない

   ```java
       public void testMultiplication() {
           Dollar five = new Dollar(5);
           Dollar product = five.times(2);
           assertEquals(10, product.amount);
           product = five.times(3);
           assertEquals(15, product.amount);
       }
   ```

2. new Dollar(10) や new Dollar(15) といったコードでは、Dollar のオブジェクトを返すという意図が読み取れるようにする

   ```java
       public void testMultiplication() {
           Dollar five = new Dollar(5);
           Dollar product = five.times(2);
           assertEquals(new Dollar(10), product);
           product = five.times(3);
           assertEquals(new Dollar(15), product);
       }
   ```

3. product を five.times(2) や five.times(3) に変更し、意図をわかりやすくする

   ```java
       public void testMultiplication() {
           Dollar five = new Dollar(5);
           assertEquals(new Dollar(10), five.times(2));
           assertEquals(new Dollar(15), five.times(3));
       }
   ```

4. amount を他クラスが呼び出すこともなくなったので、private にする。

   ```java
   package money;

   public class Dollar {
       private int amount;

       Dollar(int amount) {
           this.amount = amount;
       }

       Dollar times(int multiplier) {
           return new Dollar(amount * multiplier);
       }

       @Override
       public boolean equals(Object object) {
           Dollar dollar = (Dollar) object;
           return amount == dollar.amount;
       }
   }
   ```

### 結果

- 現在の ToDo リスト
  - null との比較や、他クラスとのオブジェクトとの比較等も求められがちだが、現状不要なので、ToDo に追加しておくのみとする
    - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
    - [x] 5USD \* 2 = 10USD
    - [x] amount を private にする
    - [x] Dollar の副作用どうする？
    - [ ] Money の丸め処理どうする？
    - [x] equals() メソッドを実装する
    - [ ] hashCode() メソッドを実装する
    - [ ] null との等価性比較
    - [ ] 他クラスとのオブジェクトとの等価性比較

# 第 5 章 原則をあえて破るとき

## とりあえずテストを通す、その後でリファクタリングをしっかり行う

### 今回の目的

1. 現状の ToDo リスト

   - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
   - [x] 5USD \* 2 = 10USD
   - [x] amount を private にする
   - [x] Dollar の副作用どうする？
   - [ ] Money の丸め処理どうする？
   - [x] equals() メソッドを実装する
   - [ ] hashCode() メソッドを実装する
   - [ ] null との等価性比較
   - [ ] 他クラスとのオブジェクトとの等価性比較

2. 何に取り組むか

   - 本当は、5USD + 10CHF = 10USD (レートが 2:1 の場合) に挑戦したいが、まだ挑める気がしない
   - スモールステップを踏む必要がある
   - Todo に 5CHF \* 2 = 10CHF を追加し一旦これをクリアする

### とりあえず、テストケースを追加する

- 以下のテストケースを追加する

  ```java
      @Test
      public void testFrancMultiplication() {
          Franc five = new Franc(5);
          assertEquals(new Franc(10), five.times(2));
          assertEquals(new Franc(15), five.times(3));
      }
  ```

- これにより、Franc クラスが必要であることが分かる
- すでに、Dollar のための public void testMultiplication() があることから、これはテストコードの重複ではないかと思われるかもしれない
- しかし、テスト駆動開発においては、以下のサイクルを回し続けることが大切だということを思い出そう
  1. 小さいテストを１つ書く
  2. 全てのテストを実行し、１つ失敗することを確認する
  3. 失敗を解消するための小さい変更を行う
  4. 再テストを行い、すべて成功することを確認する
  5. リファクタリングを行い、重複を除去する
- 大切なのは、このサイクルの 4 までをできるだけ早く終わらせ、5 をしっかりと行うことである
- よって、今回の場合も、一旦重複を許し、5 をしっかりと行うことを優先する

### とりあえず、Franc クラスを作成する

- 以下のように、Franc クラスを作成する
- ほぼ、Dollar クラスと同じである

  ```java
    package money;

    public class Franc {
        private int amount;

        Franc(int amount) {
            this.amount = amount;
        }

        Franc times(int multiplier) {
            return new Franc(amount * multiplier);
        }

        @Override
        public boolean equals(Object object) {
            Franc franc = (Franc) object;
            return amount == franc.amount;
        }
    }
  ```

### 現在の ToDo リスト

- 現在の ToDo リストは以下の通りである
- 一旦、Dollar と Franc の重複を許した代わりに、この重複を取り除くこと等をタスクとして受け入れた
  - [ ] 5USD + 10CHF = 10USD (レートが 2:1 の場合)
  - [x] 5USD \* 2 = 10USD
  - [x] amount を private にする
  - [x] Dollar の副作用どうする？
  - [ ] Money の丸め処理どうする？
  - [x] equals() メソッドを実装する
  - [ ] hashCode() メソッドを実装する
  - [ ] null との等価性比較
  - [ ] 他クラスとのオブジェクトとの等価性比較
  - [x] 5CHF \* 2 = 10CHF
  - [ ] Dollar と Franc の重複を除去する
  - [ ] equals() の一般化
  - [ ] times() の一般化
