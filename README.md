Mixin
====
<!---freshmark shields
output = [
	link(image('Release notes', 'https://img.shields.io/maven-metadata/v?label=changelog&metadataUrl=https%3A%2F%2Fjcenter.bintray.com%2F' + '{{group}}'.replaceAll("\\.", "%2F") + '%2F{{name}}%2Fmaven-metadata.xml'), '{{releaseNotesPath}}'),
	link(shield('Maven artifact', 'jcenter', '{{name}}', 'blue'), 'https://bintray.com/{{bintrayrepo}}/{{name}}/view'),
	link(image('License', 'https://img.shields.io/github/license/{{organisation}}/{{name}}.svg'), 'LICENSE') + '  ',
	link(image('Travis CI', 'https://travis-ci.org/{{organisation}}/{{name}}.svg'), 'https://travis-ci.org/{{organisation}}/{{name}}'),
	link(image('Coverage', 'https://img.shields.io/lgtm/alerts/g/{{organisation}}/{{name}}'), 'https://lgtm.com/projects/g/{{organisation}}/{{name}}'),
	link(image('Coverage', 'https://img.shields.io/codecov/c/github/{{organisation}}/{{name}}.svg'), 'https://codecov.io/gh/{{organisation}}/{{name}}/') + '  ',
	link(image('Discord chat', 'https://img.shields.io/discord/{{discordId}}?logo=discord'), '{{discordInvite}}'),
	].join('\n');
-->
[![Release notes](https://img.shields.io/maven-metadata/v?label=changelog&metadataUrl=https%3A%2F%2Fjcenter.bintray.com%2Forg%2Fminimallycorrect%2Fmixin%2FMixin%2Fmaven-metadata.xml)](docs/release-notes.md)
[![Maven artifact](https://img.shields.io/badge/jcenter-Mixin-blue.svg)](https://bintray.com/minimallycorrect/minimallycorrectmaven/Mixin/view)
[![License](https://img.shields.io/github/license/MinimallyCorrect/Mixin.svg)](LICENSE)  
[![Travis CI](https://travis-ci.org/MinimallyCorrect/Mixin.svg)](https://travis-ci.org/MinimallyCorrect/Mixin)
[![Coverage](https://img.shields.io/lgtm/alerts/g/MinimallyCorrect/Mixin)](https://lgtm.com/projects/g/MinimallyCorrect/Mixin)
[![Coverage](https://img.shields.io/codecov/c/github/MinimallyCorrect/Mixin.svg)](https://codecov.io/gh/MinimallyCorrect/Mixin/)  
[![Discord chat](https://img.shields.io/discord/313371711632441344?logo=discord)](https://discord.gg/YrV3bDm)
<!---freshmark /shields -->

A lightweight java Mixin-style patching implementation.

Reimplementation of the Mixin patcher used in [1.4.7 TickThreading builds.](https://github.com/nallar/TickThreading).
Now with 100% less regex-based Java parsing, thanks to [Java Parser](https://github.com/javaparser/javaparser).

Mixin can be applied to source files or compiled bytecode, from source files or compiled bytecode.  
Cross-applications (source to bytecode) / (bytecode to source) do not currently support all features fully due to limitations in JavaTransformer's CodeFragment feature.
