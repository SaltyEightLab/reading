現場で役立つシステム設計の原則
Chapter 2 場合分けのロジックを精里する
** プログラムを複雑にする「場合分け」のコード 区分ごとのクラスのインスタンスを生成する **

# 場合分けのロジックを整理する

if/ else を使った処理を場合分けするコードは、ロジックが複雑になり、可読性が低くなりがちである。
そうならないために、有効なのがポリモーフィズム（多態性）の利用である。
例えば、以下の様な例を想定する

- ポケモンへの指示を送る。
  - そのポケモンが炎タイプなら、火のこが発動する
  - そのポケモンが水タイプなら、水てっぽうが発動する
  - そのポケモンが草タイプなら、はっぱカッターが発動する

```Java
public class Main {
    public static void main(String[] args) {
        Pokemon pokemon = new Pokemon(Hitokage);
        pokemon.attack();
    }
}

public class Pokemon {
    public void attack() {
        if(isHitokage()) {
            System.out.println("火の粉");
        } else if(isZenigame()) {
            System.out.println("水てっぽう");
        } else if(isFushigidane()) {
            System.out.println("はっぱカッター");
        }
    }
}
```

見ての通り、if/else を使ったことで、大変読みにくくなってしまった。
しかし、ポリモーフィズムを取り入れることで、以下のようになる。

```Java

public class Main {
    public static void main(String[] args) {
        Pokemon pokemon = new Hitokage();
        pokemon.attack();
    }
}

public interface Pokemon {
    public void attack();
}

public class Hitokage implements Pokemon {
    public void attack() {
        System.out.println("火の粉");
    }
}

public class Zenigame implements Pokemon {
    public void attack() {
        System.out.println("水てっぽう");
    }
}

public class Fushigidane implements Pokemon {
    public void attack() {
        System.out.println("はっぱカッター");
    }
}


```

コードから if/ else がなくなり、attack()メソッドがシンプルになった。
次に、Hitokage, Zenigame, Fushigidane インスタンスの生成について、以下の様な場合を考えてみる。

- インスタンスを生成する。
  - 引数が FireType なら、Hitokage インスタンスを生成する。
  - 引数が WaterType なら、Zenigame インスタンスを生成する。
  - 引数が GrassType なら、Fushigidane インスタンスを生成する。

```Java
public class Main {
    public static void main(String[] args) {
        Pokemon pokemon = OkidoHakase.presentPokemon("FireType");
        pokemon.attack();
    }
}

public class OkidoHakase  {
    public static Pokemon presentPokemon(String type) {
        if(type.equals("FireType")) {
            return new Hitokage();
        } else if(type.equals("WaterType")) {
            return new Zenigame();
        } else if(type.equals("GrassType")) {
            return new Fushigidane();
        }
    }
}

```

やはり、インスタンスの生成ロジックにどうしても、if/else を使うことになってしまう。
しかし、ちょっとした工夫でこの if/else をなくすことができる。

```Java
public class Main {
    public static void main(String[] args) {
        Pokemon pokemon = OkidoHakase.presentPokemon("FireType");
        pokemon.attack();
    }
}

public class OkidoHakase  {
    static Map<String, Pokemon> pokemonLineUp;

    static {
        pokemonLineUp = new HashMap<>();
        pokemonLineUp.put("FireType", new Hitokage());
        pokemonLineUp.put("WaterType", new Zenigame());
        pokemonLineUp.put("GrassType", new Fushigidane());
    }
    public static Pokemon presentPokemon(String type) {
        return pokemonLineUp.get(type);
    }
}

```

上記のように、Map の中にキーとインスタンスを紐づけて入れておき、引数のキーを利用してインスタンスを取得するメソッドを用意しておくことで、インスタンスの生成においても if/ else 使用しなくても済むようになる。

なお、上記例において、static ブロックを使用しているのは

- そもそも、presentPokemon メソッドが static メソッドであるため、コンストラクタによる初期化では遅い。
- pokemonLineUp 変数の定義に static ブロック内の処理を入れ込むことも可能だが、それでは、変数の定義という責務を超えた処理となり、可読性が落ちる。
  という理由からである。

# Java の列挙型を使えばもっと簡単

- Java の列挙型(enum)は、単なる定数のリストではなく、その定数にインスタンスを紐づけたり、コンストラクタ、インスタンス変数、メソッドなどを定義することができる。
- その一例が以下
- 以下のような設計を区分オブジェクトという。

```Java

public class Main {
    public static void main(String[] args) {
        Pokemon pokemon = PokemonType.FIRE.getPokemon();
        pokemon.attack();
    }
}

public enum PokemonType {
    FIRE(new Hitokage()),
    WATER(new Zenigame()),
    GRASS(new Fushigidane());

    private final Pokemon pokemon;

    PokemonType(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }
}

```

# enum 型とは「線路の切り替え装置」のようなものである

Java の enum 型（列挙型）は一見するとただの定数の羅列に見えますが、実はそれ以上に便利で強力な機能を備えています。

この記事では、enum 型の理解を深めるために、わかりやすく鉄道の「線路の切り替え」に例えて解説します。

---

## enum を鉄道の線路に例えて考える

東京の鉄道網を想像してみましょう。例えば、新宿線、東京線、池袋線といった複数の路線が存在しています。これらの路線をそれぞれ Java のクラスと考えてみます。

```java
// 基底となる路線インターフェース
interface Line {
    void run();
}

// 各路線クラス
class ShinjukuLine implements Line {
    public void run() { System.out.println("新宿線を走行中"); }
}

class TokyoLine implements Line {
    public void run() { System.out.println("東京線を走行中"); }
}

class IkebukuroLine implements Line {
    public void run() { System.out.println("池袋線を走行中"); }
}
```

これらはそれぞれ異なる路線クラスであり、バラバラに存在しています。しかし、このままでは特定の路線に簡単に切り替えることができません。例えば、ひとつの路線から他の路線に移動するためには、何らかの切り替え装置が必要です。

---

## enum 型は「線路の切り替え装置」として働く

ここで役に立つのが enum 型です。enum は複数の定数を用意することができ、それぞれに対応した路線のインスタンスを紐付けて管理できます。

```java
enum LineSwitcher {
    SHINJUKU(new ShinjukuLine()),
    TOKYO(new TokyoLine()),
    IKEBUKURO(new IkebukuroLine());

    private Line line;

    private LineSwitcher(Line line) {
        this.line = line;
    }

    public Line getLine() {
        return line;
    }
}
```

このように、enum 型を定義するときにコンストラクタを利用して、それぞれの定数に路線クラスのインスタンスを紐付けることができます。

---

## enum による切り替えの使い方

enum を使えば、条件分岐を使わずに簡単に目的の路線に切り替えることができます。

```java
class TrainController {
    public static void main(String[] args) {
        LineSwitcher selectedLine = LineSwitcher.SHINJUKU; // 新宿線を選択
        selectedLine.getLine().run(); // 「新宿線を走行中」と表示

        selectedLine = LineSwitcher.TOKYO; // 東京線に切り替え
        selectedLine.getLine().run(); // 「東京線を走行中」と表示
    }
}
```

---

## enum 型のメリット

enum 型を「線路の切り替え装置」として使うことには以下のようなメリットがあります。

- **視認性の向上**：どの路線があるのか一目瞭然になる
- **簡単な拡張性**：新たな路線を追加するときは enum に定数を追加するだけで済む
- **保守性・可読性の向上**：if や switch などの条件分岐を使わずにコードがシンプルにまとまる

---

## まとめ

Java の enum 型は単なる定数の羅列ではなく、それぞれの定数に処理を紐付け、切り替え装置として利用することができます。鉄道の路線と線路の切り替えというイメージを持つことで、enum 型の本質が直感的に理解できるようになります。

ぜひ、enum を設計に取り入れて、シンプルで美しいコードを実現してみてください。
