parser grammar KSMLParser;

import GLSLParser;

options {
	tokenVocab = KSMLLexer;
}

ksmlTranslationUnit : moduleMeta glVersionMeta requiresMeta* ksmlDeclaration* EOF;

moduleMeta : AT MODULE IDENTIFIER;

requiresMeta : AT REQUIRES IDENTIFIER;

glVersionMeta : AT GL_VERSION VERSION_NUMBER glVersionIdent?;

glVersionIdent : GL_PROFILE_CORE | GL_PROFILE_COMPAT;

ksmlDeclaration : declarationMeta* codeBlock;

declarationMeta :
    exportMeta
    | glRequiresMeta
    | featureMeta;

exportMeta : AT EXPORT;

glRequiresMeta : AT GL_REQUIRES VERSION_NUMBER glVersionIdent?;

featureMeta : AT FEATURE IDENTIFIER;

codeBlock : AT CODE TRIPLE_QUOTE function_definition TRIPLE_QUOTE;
