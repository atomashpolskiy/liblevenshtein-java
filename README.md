# liblevenshtein

## Java

### A library for generating Finite State Transducers based on Levenshtein Automata.

[![Maven Central][maven-version-badge]][maven-repo]
[![Bintray][bintray-badge]][bintray-repo]
[![Reference Status][maven-refs-badge]][maven-refs]
[![License][license-badge]][license]
[![Build Status][build-status-badge]][travis-ci]
[![Coverage Status][coverage-badge]][coveralls]
[![Coverity Scan Build Status][static-analysis-badge]][coverity]
[![Dependency Status][dependency-status-badge]][versioneye]
[![Gitter][gitter-badge]][gitter-channel]

Levenshtein transducers accept a query term and return all terms in a
dictionary that are within n spelling errors away from it. They constitute a
highly-efficient (space _and_ time) class of spelling correctors that work very
well when you do not require context while making suggestions.  Forget about
performing a linear scan over your dictionary to find all terms that are
sufficiently-close to the user's query, using a quadratic implementation of the
[Levenshtein distance][wikipedia-levenshtein-distance] or
[Damerau-Levenshtein distance][wikipedia-damerau-levenshtein-distance], these
babies find _all_ the terms from your dictionary in linear time _on the length
of the query term_ (not on the size of the dictionary, on the length of the
query term).

If you need context, then take the candidates generated by the transducer as a
starting place, and plug them into whatever model you're using for context (such
as by selecting the sequence of terms that have the greatest probability of
appearing together).

For a quick demonstration, please visit the [Github Page, here][live-demo].
There's also a command-line interface, [liblevenshtein-java-cli][java-cli].
Please see its [README.md][java-cli-readme] for acquisition and usage information.

The library is currently written in Java, CoffeeScript, and JavaScript, but I
will be porting it to other languages, soon.  If you have a specific language
you would like to see it in, or package-management system you would like it
deployed to, let me know.

### Branches

|                            Branch | Description                                 |
| ---------------------------------:|:------------------------------------------- |
|           [master][master-branch] | Latest, development source.                 |
|         [release][release-branch] | Latest, release source.                     |

### Documentation

When it comes to documentation, you have several options:
- [Wiki][wiki]
- [Javadoc][javadoc]
- [Source Code][tagged-source]

### Basic Usage:

### Minimum Java Version

liblevenshtein has been developed against Java &ge; 1.8.
It will not work with prior versions.

#### Installation

##### Maven

```xml
<dependency>
  <groupId>com.github.universal-automata</groupId>
  <artifactId>liblevenshtein</artifactId>
  <version>2.2.3-alpha.9</version>
</dependency>
```

##### Apache Buildr

```ruby
'com.github.universal-automata:liblevenshtein:jar:2.2.3-alpha.9'
```

##### Apache Ivy

```xml
<dependency org="com.github.universal-automata" name="liblevenshtein" rev="2.2.3-alpha.9" />
```

##### Groovy Grape

```groovy
@Grapes(
@Grab(group='com.github.universal-automata', module='liblevenshtein', version='2.2.3-alpha.9')
)
```

##### Gradle / Grails

```groovy
compile 'com.github.universal-automata:liblevenshtein:2.2.3-alpha.9'
```

##### Scala SBT

```scala
libraryDependencies += "com.github.universal-automata" % "liblevenshtein" % "2.2.3-alpha.9"
```

##### Leiningen

```clojure
[com.github.universal-automata/liblevenshtein "2.2.3-alpha.9"]
```

##### Git

```
% git clone --progress git@github.com:universal-automata/liblevenshtein-java.git
Cloning into 'liblevenshtein-java'...
remote: Counting objects: 3879, done.        
remote: Compressing objects: 100% (511/511), done.        
remote: Total 3879 (delta 373), reused 0 (delta 0), pack-reused 3317        
Receiving objects: 100% (3879/3879), 2.83 MiB | 1.81 MiB/s, done.
Resolving deltas: 100% (1988/1988), done.
Checking connectivity... done.

% cd liblevenshtein-java
% git pull --progress
Already up-to-date.

% git fetch --progress --tags
% git checkout --progress master
Already on 'master'
Your branch is up-to-date with 'origin/master'.

% git submodule init
% git submodule update

```

### Usage

Let's say you have the following content in a plain text file called,
[top-20-most-common-english-words.txt][top-20-most-common-english-words.txt]
(note that the file has one term per line):

```
the
be
to
of
and
a
in
that
have
I
it
for
not
on
with
he
as
you
do
at
```

The following provides you a way to query its content:

```java
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.dylon.liblevenshtein.collection.dawg.SortedDawg;
import com.github.dylon.liblevenshtein.levenshtein.Algorithm;
import com.github.dylon.liblevenshtein.levenshtein.Candidate;
import com.github.dylon.liblevenshtein.levenshtein.ITransducer;
import com.github.dylon.liblevenshtein.levenshtein.factory.TransducerBuilder;
import com.github.dylon.liblevenshtein.serialization.PlainTextSerializer;
import com.github.dylon.liblevenshtein.serialization.ProtobufSerializer;
import com.github.dylon.liblevenshtein.serialization.Serializer;

// ...

final SortedDawg dictionary;
final Path dictionaryPath =
  Paths.get("/path/to/top-20-most-common-english-words.txt");
try (final InputStream stream = Files.newInputStream(dictionaryPath)) {
  // The PlainTextSerializer constructor accepts an optional boolean specifying
  // whether the dictionary is already sorted lexicographically, in ascending
  // order.  If it is sorted, then passing true will optimize the construction
  // of the dictionary; you may pass false whether the dictionary is sorted or
  // not (this is the default and safest behavior if you don't know whether the
  // dictionary is sorted).
  final Serializer serializer = new PlainTextSerializer(false);
  dictionary = serializer.deserialize(SortedDawg.class, stream);
}

final ITransducer<Candidate> transducer = new TransducerBuilder()
  .dictionary(dictionary)
  .algorithm(Algorithm.TRANSPOSITION)
  .defaultMaxDistance(2)
  .includeDistance(true)
  .build();

for (final String queryTerm : new String[] {"foo", "bar"}) {
  System.out.println(
    "+-------------------------------------------------------------------------------");
  System.out.printf("| Spelling Candidates for Query Term: \"%s\"%n", queryTerm);
  System.out.println(
    "+-------------------------------------------------------------------------------");
  for (final Candidate candidate : transducer.transduce(queryTerm)) {
    System.out.printf("| d(\"%s\", \"%s\") = [%d]%n",
      queryTerm,
      candidate.term(),
      candidate.distance());
  }
}

// +-------------------------------------------------------------------------------
// | Spelling Candidates for Query Term: "foo"
// +-------------------------------------------------------------------------------
// | d("foo", "do") = [2]
// | d("foo", "of") = [2]
// | d("foo", "on") = [2]
// | d("foo", "to") = [2]
// | d("foo", "for") = [1]
// | d("foo", "not") = [2]
// | d("foo", "you") = [2]
// +-------------------------------------------------------------------------------
// | Spelling Candidates for Query Term: "bar"
// +-------------------------------------------------------------------------------
// | d("bar", "a") = [2]
// | d("bar", "as") = [2]
// | d("bar", "at") = [2]
// | d("bar", "be") = [2]
// | d("bar", "for") = [2]

// ...
```

If you want to serialize your dictionary to a format that's easy to read later,
do the following:

```java
final Path serializedDictionaryPath =
  Paths.get("/path/to/top-20-most-common-english-words.protobuf.bytes");
try (final OutputStream stream = Files.newOutputStream(serializedDictionaryPath)) {
  final Serializer serializer = new ProtobufSerializer();
  serializer.serialize(dictionary, stream);
}
```

Then, you can read the dictionary later, in much the same way you read the plain
text version:

```java
final SortedDawg deserializedDictionary;
try (final InputStream stream = Files.newInputStream(serializedDictionaryPath)) {
  final Serializer serializer = new ProtobufSerializer();
  deserializedDictionary = serializer.deserialize(SortedDawg.class, stream);
}
```

Serialization is not restricted to dictionaries, you may also (de)serialize
transducers.

Please see the [wiki][wiki] for more details.

### Reference

This library is based largely on the work of
[Stoyan Mihov][stoyan-mihov], [Klaus Schulz][klaus-schulz], and
Petar Nikolaev Mitankin:
[Fast String Correction with Levenshtein-Automata][fast-string-correction-2002].
For more information, please see the [wiki][wiki].

[top-20-most-common-english-words.txt]: https://raw.githubusercontent.com/universal-automata/liblevenshtein-java/2.2.3-alpha.9/src/test/resources/top-20-most-common-english-words.txt "top-20-most-common-english-words.txt"

[maven-version-badge]: https://maven-badges.herokuapp.com/maven-central/com.github.dylon/liblevenshtein/badge.svg
[maven-repo]: https://maven-badges.herokuapp.com/maven-central/com.github.dylon/liblevenshtein
[bintray-repo]: https://bintray.com/universal-automata/liblevenshtein/liblevenshtein-java/_latestVersion
[bintray-badge]: https://api.bintray.com/packages/universal-automata/liblevenshtein/liblevenshtein-java/images/download.svg
[maven-refs-badge]: https://www.versioneye.com/java/com.github.dylon:liblevenshtein/reference_badge.svg
[maven-refs]: https://www.versioneye.com/java/com.github.dylon:liblevenshtein/references
[license-badge]: https://img.shields.io/github/license/universal-automata/liblevenshtein-java.svg
[license]: https://raw.githubusercontent.com/universal-automata/liblevenshtein-java/master/LICENSE
[build-status-badge]: https://travis-ci.org/universal-automata/liblevenshtein-java.svg?branch=master
[travis-ci]: https://travis-ci.org/universal-automata/liblevenshtein-java
[coverage-badge]: https://coveralls.io/repos/github/universal-automata/liblevenshtein-java/badge.svg?branch=master
[coveralls]: https://coveralls.io/github/universal-automata/liblevenshtein-java?branch=master
[static-analysis-badge]: https://img.shields.io/coverity/scan/8476.svg
[coverity]: https://scan.coverity.com/projects/universal-automata-liblevenshtein-java
[dependency-status-badge]: https://www.versioneye.com/user/projects/570345d4fcd19a0051853d99/badge.svg
[versioneye]: https://www.versioneye.com/user/projects/570345d4fcd19a0051853d99
[gitter-badge]: https://badges.gitter.im/universal-automata/liblevenshtein-java.svg
[gitter-channel]: https://gitter.im/universal-automata/liblevenshtein-java?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge

[liblevenshtein-java][github-repo] is maintained by[@dylon][github-author] ([dylon.devo+liblevenshtein-java@gmail.com][github-email])

[coursera-automata]: https://class.coursera.org/automata "Jeffrey Ullman (Coursera)"
[coursera-compilers]: https://class.coursera.org/compilers "Alex Aiken (Coursera)"
[coursera-nlp]: https://class.coursera.org/nlp "Dan Jurafsky and Chris Manning (Coursera)"
[damn-cool-algos-levenshtein-automata-2010]: http://blog.notdot.net/2010/07/Damn-Cool-Algorithms-Levenshtein-Automata "Nick Johnson (2010)"
[dict-compress-dawg-2011]: http://stevehanov.ca/blog/index.php?id=115 "Steve Hanov (2011)"
[fast-easy-correct-trie-2011]: http://stevehanov.ca/blog/index.php?id=114 "Steve Hanov (2011)"
[fast-string-correction-2002]: http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.16.652 "Klaus Schulz and Stoyan Mihov (2002)"
[incremental-construction-dawg-2000]: http://dl.acm.org/citation.cfm?id=971842 "Jan Daciuk, Bruce W. Watson, Stoyan Mihov, and Richard E. Watson (2000)"
[klaus-schulz]: http://www.cis.uni-muenchen.de/people/schulz.html "Klaus Schulz"
[lucene-fuzzy-2011]: http://blog.mikemccandless.com/2011/03/lucenes-fuzzyquery-is-100-times-faster.html "Michael McCandless (2011)"
[moman]: https://sites.google.com/site/rrettesite/moman "Moman"
[rao-li]: http://www.usca.edu/math/~mathdept/rli/ "Dr. Rao Li"
[stoyan-mihov]: http://www.lml.bas.bg/~stoyan/ "Stoyan Mihov"
[universal-automata-2005]: http://www.fmi.uni-sofia.bg/fmi/logic/theses/mitankin-en.pdf "Petar Nikolaev Mitankin (2005)"
[usca]: http://web.usca.edu/ "University of South Carolina Aiken"

[live-demo]: http://universal-automata.github.io/liblevenshtein/

[github-author]: https://github.com/dylon "Dylon Edwards <dylon.devo+liblevenshtein-java@gmail.com>"
[github-demo]: http://universal-automata.github.io/liblevenshtein/ "liblevenshtein demo"
[github-email]: mailto:dylon.devo+liblevenshtein-java@gmail.com "Dylon Edwards <dylon.devo+liblevenshtein-java@gmail.com>"
[github-repo]: https://github.com/universal-automata/liblevenshtein-java/ "universal-automata/liblevenshtein-java"

[wikipedia-damerau-levenshtein-distance]: https://en.wikipedia.org/wiki/Damerau%E2%80%93Levenshtein_distance "Damerau–Levenshtein distance"
[wikipedia-levenshtein-distance]: https://en.wikipedia.org/wiki/Levenshtein_distance "Levenshtein distance"

[master-branch]: https://github.com/universal-automata/liblevenshtein-java/tree/master
[release-branch]: https://github.com/universal-automata/liblevenshtein-java/tree/release

[wiki]: http://universal-automata.github.io/liblevenshtein-java/docs/wiki/2.2.3-alpha.9/index.md "liblevenshtein 2.2.3-alpha.9 Wiki"
[javadoc]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/index.html "liblevenshtein 2.2.3-alpha.9 API"
[tagged-source]: https://github.com/universal-automata/liblevenshtein-java/tree/2.2.3-alpha.9/src "liblevenshtein 2.2.3-alpha.9"

[java-lib]: https://github.com/universal-automata/liblevenshtein-java "liblevenshtein-java"
[java-cli]: https://github.com/universal-automata/liblevenshtein-java-cli "liblevenshtein-java-cli"
[java-cli-readme]: https://github.com/universal-automata/liblevenshtein-java-cli/blob/master/README.md "liblevenshtein-java-cli, README.md"

[javadoc/Iterable]: https://docs.oracle.com/javase/1.8/docs/api/java/lang/Iterable.html?is-external=true "java.lang.Iterable"
[javadoc/Iterator.next()]: https://docs.oracle.com/javase/1.8/docs/api/java/util/Iterator.html#next-- "java.util.Iterator.next()"
[javadoc/Iterator]: https://docs.oracle.com/javase/1.8/docs/api/java/util/Iterator.html "java.util.Iterator"
[javadoc/String]: https://docs.oracle.com/javase/1.8/docs/api/java/lang/String.html "java.lang.String"

[javadoc/Algorithm.MERGE_AND_SPLIT]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/Algorithm.html#MERGE_AND_SPLIT "Algorithm.MERGE_AND_SPLIT"
[javadoc/Algorithm.STANDARD]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/Algorithm.html#STANDARD "Algorithm.STANDARD"
[javadoc/Algorithm.TRANSPOSITION]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/Algorithm.html#TRANSPOSITION "Algorithm.TRANSPOSITION"
[javadoc/ICandidateCollection]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/ICandidateCollection.html "ICandidateCollection"
[javadoc/ITransducer.transduce(String)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/ITransducer.html#transduce-java.lang.String- "ITransducer.transduce(String):ICandidateCollection"
[javadoc/ITransducer.transduce(String,int)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/ITransducer.html#transduce-java.lang.String-int- "ITransducer.transduce(String,int):ICandidateCollection"
[javadoc/MemoizedMergeAndSplit.between(String,String)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/distance/MemoizedMergeAndSplit.html "MemoizedMergeAndSplit.between(String,String):int"
[javadoc/MemoizedStandard.between(String,String)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/distance/MemoizedStandard.html "MemoizedStandard.between(String,String):int"
[javadoc/MemoizedTransposition.between(String,String)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/distance/MemoizedTransposition.html "MemoizedTransposition.between(String,String):int"
[javadoc/TransducerBuilder.algorithm(Algorithm)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.html#algorithm-com.github.dylon.liblevenshtein.levenshtein.Algorithm- "TransducerBuilder.algorithm(Algorithm):TransducerBuilder"
[javadoc/TransducerBuilder.build()]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.html#build-- "TransducerBuilder.build():ITransducer"
[javadoc/TransducerBuilder.defaultMaxDistance(int)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.html#defaultMaxDistance-int- "TransducerBuilder.defaultMaxDistance(int):TransducerBuilder"
[javadoc/TransducerBuilder.dictionary(Collection)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.html#dictionary-java.util.Collection- "TransducerBuilder.dictionary(Collection):TransducerBuilder"
[javadoc/TransducerBuilder.dictionary(Collection,boolean)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.html#dictionary-java.util.Collection-boolean- "TransducerBuilder.dictionary(Collection,boolean):TransducerBuilder"
[javadoc/TransducerBuilder.includeDistance(boolean)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.html#includeDistance-boolean- "TransducerBuilder.includeDistance(boolean):TransducerBuilder"
[javadoc/TransducerBuilder.maxCandidates(int)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/2.2.3-alpha.9/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.html#maxCandidates-int- "TransducerBuilder.maxCandidates(int):TransducerBuilder"

[src/Candidate]: https://github.com/universal-automata/liblevenshtein-java/blob/master/src/main/java/com/github/dylon/liblevenshtein/levenshtein/Candidate.java "Candidate.java"
[src/ITransducer]: https://github.com/universal-automata/liblevenshtein-java/blob/2.2.3-alpha.9/src/main/java/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.java "TransducerBuilder.java"
[src/TransducerBuilder.java]: https://github.com/universal-automata/liblevenshtein-java/blob/2.2.3-alpha.9/src/main/java/com/github/dylon/liblevenshtein/levenshtein/factory/TransducerBuilder.java "TransducerBuilder.java"
[src/build.gradle]: https://github.com/universal-automata/liblevenshtein-java/blob/2.2.3-alpha.9/build.gradle "build.gradle"

[top-20-most-common-english-words.txt]: https://raw.githubusercontent.com/universal-automata/liblevenshtein-java/2.2.3-alpha.9/src/test/resources/top-20-most-common-english-words.txt "top-20-most-common-english-words.txt"
