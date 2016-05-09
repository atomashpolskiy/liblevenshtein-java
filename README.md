# liblevenshtein

## Java

### A library for generating Finite State Transducers based on Levenshtein Automata.

[![Maven Central][maven-badge]][maven-link]
[![Artifactory][artifactory-badge]][artifactory-link]
[![License][license-badge]][license-link]
[![Build Status][travis-ci-badge]][travis-ci-link]
[![Codacy Badge][codacy-badge]][codacy-link]
[![Coverage Status][coveralls-badge]][coveralls-link]
[![Coverity Scan Build Status][coverity-badge]][coverity-link]
[![Dependency Status][versioneye-badge]][versioneye-link]
[![Gitter][gitter-badge]][gitter-channel]
[![Bountysource][bountysource-badge]][bountysource-link]

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

[![][bintray-watch-img]][bintray-watch-link]

[![Twitter Follow][twitter-badge]][twitter-link]

### Branches

|                            Branch | Description                            |
| ---------------------------------:|:-------------------------------------- |
|           [master][master-branch] | Latest, development source             |
|         [release][release-branch] | Latest, release source                 |
| [release-3.x][release-branch-3.x] | Latest, release source for version 3.x |
| [release-2.x][release-branch-2.x] | Latest, release source for version 2.x |

### Project Management

Issues are managed on [waffle.io][waffle-io-link].  Below you will find a graph
on the rate at which I've been closing them.

[![Throughput Graph][waffle-io-throughput-graph]][waffle-io-throughput-link]

[![Tasks ready to be worked on][waffle-io-ready-badge]][waffle-io-link]
[![Tasks being worked on][waffle-io-in-progress-badge]][waffle-io-link]

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

##### Latest, Development Release

Add a Maven dependency on [Artifactory][artifactory-repo].  For example, in a
[Gradle][gradle-home] project, you would modify your `repositories` as follows:

```groovy
repositories {
  maven {
    url 'https://oss.jfrog.org/artifactory/oss-release-local'
  }
}
```

##### Latest, Stable Release

Add a Maven dependency on one of the following:
- [Maven Central][maven-repo]
- [JCenter][jcenter-repo]
- [Bintray][bintray-repo]

##### Maven

```xml
<dependency>
  <groupId>com.github.universal-automata</groupId>
  <artifactId>liblevenshtein</artifactId>
  <version>3.0.0-beta.1</version>
</dependency>
```

##### Apache Buildr

```ruby
'com.github.universal-automata:liblevenshtein:jar:3.0.0-beta.1'
```

##### Apache Ivy

```xml
<dependency org="com.github.universal-automata" name="liblevenshtein" rev="3.0.0-beta.1" />
```

##### Groovy Grape

```groovy
@Grapes(
@Grab(group='com.github.universal-automata', module='liblevenshtein', version='3.0.0-beta.1')
)
```

##### Gradle / Grails

```groovy
compile 'com.github.universal-automata:liblevenshtein:3.0.0-beta.1'
```

##### Scala SBT

```scala
libraryDependencies += "com.github.universal-automata" % "liblevenshtein" % "3.0.0-beta.1"
```

##### Leiningen

```clojure
[com.github.universal-automata/liblevenshtein "3.0.0-beta.1"]
```

##### Git

```
% git clone --progress git@github.com:universal-automata/liblevenshtein-java.git
Cloning into 'liblevenshtein-java'...
remote: Counting objects: 6620, done.        
remote: Compressing objects: 100% (1032/1032), done.        
remote: Total 6620 (delta 813), reused 0 (delta 0), pack-reused 5539        
Receiving objects: 100% (6620/6620), 4.51 MiB | 4.56 MiB/s, done.
Resolving deltas: 100% (4128/4128), done.
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

import com.github.liblevenshtein.collection.dictionary.SortedDawg;
import com.github.liblevenshtein.serialization.PlainTextSerializer;
import com.github.liblevenshtein.serialization.ProtobufSerializer;
import com.github.liblevenshtein.serialization.Serializer;
import com.github.liblevenshtein.transducer.Algorithm;
import com.github.liblevenshtein.transducer.Candidate;
import com.github.liblevenshtein.transducer.ITransducer;
import com.github.liblevenshtein.transducer.factory.TransducerBuilder;

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

[top-20-most-common-english-words.txt]: https://raw.githubusercontent.com/universal-automata/liblevenshtein-java/3.0.0-beta.1/src/test/resources/top-20-most-common-english-words.txt "top-20-most-common-english-words.txt"

[artifactory-badge]: https://img.shields.io/badge/artifactory-v3.0.0--beta.1-yellow.svg?style=flat
[artifactory-link]: https://oss.jfrog.org/artifactory/webapp/#/artifacts/browse/tree/General/oss-release-local/com/github/universal-automata/liblevenshtein/3.0.0-beta.1 "Latest, development release (Artifactory)"
[bintray-badge]: https://img.shields.io/bintray/v/universal-automata/liblevenshtein/liblevenshtein-java.svg?style=flat
[bintray-link]: https://bintray.com/universal-automata/liblevenshtein/liblevenshtein-java/_latestVersion "Latest, stable release (Bintray)"
[bintray-watch-img]: https://www.bintray.com/docs/images/bintray_badge_color.png
[bintray-watch-link]: https://bintray.com/universal-automata/liblevenshtein/liblevenshtein-java/view?source=watch 'Get automatic notifications about new "liblevenshtein-java" versions'
[bountysource-badge]: https://img.shields.io/bountysource/team/universal-automata/activity.svg?style=flat
[bountysource-link]: https://www.bountysource.com/teams/universal-automata "Create and pledge bounties"
[codacy-badge]: https://api.codacy.com/project/badge/Grade/ad002473702c4d0b8532a6ba38af2010
[codacy-link]: https://www.codacy.com/app/dylon-devo-github/liblevenshtein-java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=universal-automata/liblevenshtein-java&amp;utm_campaign=Badge_Grade "Code health"
[coveralls-badge]: https://coveralls.io/repos/github/universal-automata/liblevenshtein-java/badge.svg?branch=master
[coveralls-link]: https://coveralls.io/github/universal-automata/liblevenshtein-java?branch=master "Unit test, code coverage"
[coverity-badge]: https://img.shields.io/coverity/scan/8476.svg
[coverity-link]: https://scan.coverity.com/projects/universal-automata-liblevenshtein-java "Static code analysis"
[github-tag-badge]: https://img.shields.io/github/tag/universal-automata/liblevenshtein-java.svg
[github-tag-link]: https://github.com/universal-automata/liblevenshtein-java/tags "Latest, source download"
[gitter-badge]: https://img.shields.io/gitter/room/universal-automata/liblevenshtein-java.svg?style=flat
[gitter-channel]: https://gitter.im/universal-automata/liblevenshtein-java?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge "Gitter channel (liblevenshtein-java)"
[license-badge]: https://img.shields.io/github/license/universal-automata/liblevenshtein-java.svg
[license-link]: https://raw.githubusercontent.com/universal-automata/liblevenshtein-java/master/LICENSE "MIT Licence"
[maven-badge]: https://img.shields.io/maven-central/v/com.github.universal-automata/liblevenshtein.svg
[maven-link]: http://search.maven.org/#artifactdetails%7Ccom.github.universal-automata%7Cliblevenshtein%7C2.2.3%7C "Latest, stable release (Maven Central)"
[maven-refs-badge]: https://www.versioneye.com/java/com.github.universal-automata:liblevenshtein/reference_badge.svg
[maven-refs-link]: https://www.versioneye.com/java/com.github.universal-automata:liblevenshtein/references
[travis-ci-badge]: https://travis-ci.org/universal-automata/liblevenshtein-java.svg?branch=master
[travis-ci-link]: https://travis-ci.org/universal-automata/liblevenshtein-java "Build status"
[versioneye-badge]: https://www.versioneye.com/user/projects/570345d4fcd19a0051853d99/badge.svg
[versioneye-link]: https://www.versioneye.com/user/projects/570345d4fcd19a0051853d99 "Dependency updates"

[waffle-io-ready-badge]: https://badge.waffle.io/universal-automata/liblevenshtein-java.png?label=ready&title=Ready
[waffle-io-in-progress-badge]: https://badge.waffle.io/universal-automata/liblevenshtein-java.png?label=in%20progress&title=In%20Progress
[waffle-io-throughput-graph]: https://graphs.waffle.io/universal-automata/liblevenshtein-java/throughput.svg
[waffle-io-throughput-link]: https://waffle.io/universal-automata/liblevenshtein-java/metrics/throughput
[waffle-io-link]: https://waffle.io/universal-automata/liblevenshtein-java "Project planner"

[twitter-badge]: https://img.shields.io/twitter/follow/liblevenshtein.svg?style=social&label=Twitter
[twitter-link]: https://twitter.com/liblevenshtein "Universal Automata (@liblevenshtein)"

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

[master-branch]: https://github.com/universal-automata/liblevenshtein-java/tree/master "universal-automata/liblevenshtein-java/master"
[release-branch]: https://github.com/universal-automata/liblevenshtein-java/tree/release "universal-automata/liblevenshtein-java/release"
[release-branch-3.x]: https://github.com/universal-automata/liblevenshtein-java/tree/release-3.x "universal-automata/liblevenshtein-java/release-3.x"
[release-branch-2.x]: https://github.com/universal-automata/liblevenshtein-java/tree/release-2.x "universal-automata/liblevenshtein-java/release-2.x"

[wiki]: https://github.com/universal-automata/liblevenshtein-java/blob/gh-pages/docs/wiki/3.0.0-beta.1/index.md "liblevenshtein 3.0.0-beta.1 Wiki"
[javadoc]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/index.html "liblevenshtein 3.0.0-beta.1 API"
[tagged-source]: https://github.com/universal-automata/liblevenshtein-java/tree/3.0.0-beta.1/src "liblevenshtein 3.0.0-beta.1"

[java-lib]: https://github.com/universal-automata/liblevenshtein-java "liblevenshtein-java"
[java-cli]: https://github.com/universal-automata/liblevenshtein-java-cli "liblevenshtein-java-cli"
[java-cli-readme]: https://github.com/universal-automata/liblevenshtein-java-cli/blob/master/README.md "liblevenshtein-java-cli, README.md"

[javadoc/Iterable]: https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html?is-external=true "java.lang.Iterable"
[javadoc/Iterator.next()]: https://docs.oracle.com/javase/8/docs/api/java/util/Iterator.html#next-- "java.util.Iterator.next()"
[javadoc/Iterator]: https://docs.oracle.com/javase/8/docs/api/java/util/Iterator.html "java.util.Iterator"
[javadoc/String]: https://docs.oracle.com/javase/8/docs/api/java/lang/String.html "java.lang.String"

[javadoc/Algorithm.MERGE_AND_SPLIT]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/Algorithm.html#MERGE_AND_SPLIT "Algorithm.MERGE_AND_SPLIT"
[javadoc/Algorithm.STANDARD]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/Algorithm.html#STANDARD "Algorithm.STANDARD"
[javadoc/Algorithm.TRANSPOSITION]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/Algorithm.html#TRANSPOSITION "Algorithm.TRANSPOSITION"
[javadoc/ITransducer.transduce(String)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/ITransducer.html#transduce-java.lang.String- "ITransducer.transduce(String):Iterable"
[javadoc/ITransducer.transduce(String,int)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/ITransducer.html#transduce-java.lang.String-int- "ITransducer.transduce(String,int):Iterable"
[javadoc/MemoizedMergeAndSplit.between(String,String)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/distance/MemoizedMergeAndSplit.html "MemoizedMergeAndSplit.between(String,String):int"
[javadoc/MemoizedStandard.between(String,String)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/distance/MemoizedStandard.html "MemoizedStandard.between(String,String):int"
[javadoc/MemoizedTransposition.between(String,String)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/distance/MemoizedTransposition.html "MemoizedTransposition.between(String,String):int"
[javadoc/TransducerBuilder.algorithm(Algorithm)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/factory/TransducerBuilder.html#algorithm-com.github.liblevenshtein.Algorithm- "TransducerBuilder.algorithm(Algorithm):TransducerBuilder"
[javadoc/TransducerBuilder.build()]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/factory/TransducerBuilder.html#build-- "TransducerBuilder.build():ITransducer"
[javadoc/TransducerBuilder.defaultMaxDistance(int)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/factory/TransducerBuilder.html#defaultMaxDistance-int- "TransducerBuilder.defaultMaxDistance(int):TransducerBuilder"
[javadoc/TransducerBuilder.dictionary(Collection)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/factory/TransducerBuilder.html#dictionary-java.util.Collection- "TransducerBuilder.dictionary(Collection):TransducerBuilder"
[javadoc/TransducerBuilder.dictionary(Collection,boolean)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/factory/TransducerBuilder.html#dictionary-java.util.Collection-boolean- "TransducerBuilder.dictionary(Collection,boolean):TransducerBuilder"
[javadoc/TransducerBuilder.includeDistance(boolean)]: http://universal-automata.github.io/liblevenshtein-java/docs/javadoc/3.0.0-beta.1/com/github/liblevenshtein/transducer/factory/TransducerBuilder.html#includeDistance-boolean- "TransducerBuilder.includeDistance(boolean):TransducerBuilder"

[src/Candidate]: https://github.com/universal-automata/liblevenshtein-java/blob/master/src/main/java/com/github/liblevenshtein/transducer/Candidate.java "Candidate.java"
[src/ITransducer]: https://github.com/universal-automata/liblevenshtein-java/blob/3.0.0-beta.1/src/main/java/com/github/liblevenshtein/transducer/factory/TransducerBuilder.java "TransducerBuilder.java"
[src/TransducerBuilder.java]: https://github.com/universal-automata/liblevenshtein-java/blob/3.0.0-beta.1/src/main/java/com/github/liblevenshtein/transducer/factory/TransducerBuilder.java "TransducerBuilder.java"
[src/build.gradle]: https://github.com/universal-automata/liblevenshtein-java/blob/3.0.0-beta.1/build.gradle "build.gradle"

[top-20-most-common-english-words.txt]: https://raw.githubusercontent.com/universal-automata/liblevenshtein-java/3.0.0-beta.1/src/test/resources/top-20-most-common-english-words.txt "top-20-most-common-english-words.txt"

[maven-repo]: https://repo1.maven.org/maven2 "Maven Central repository"
[jcenter-repo]: https://jcenter.bintray.com "JCenter repository"
[bintray-repo]: https://dl.bintray.com/universal-automata/liblevenshtein "Bintray repository"
[artifactory-repo]: https://oss.jfrog.org/artifactory/oss-release-local "Artifactory repository"

[gradle-home]: http://gradle.org/ "Gradle homepage"
