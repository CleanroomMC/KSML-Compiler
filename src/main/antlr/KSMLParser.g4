parser grammar KSMLParser;

import GLSLParser;

options {
	tokenVocab = KSMLLexer;
}

ksmlTranslationUnit: moduleMeta glVersionMeta externalDeclarations* EOF;

exportMeta: AT EXPORT IDENTIFIER?;

externalDeclarations:
    exportMeta?
    external_declaration;

moduleMeta: AT MODULE IDENTIFIER;

glVersionMeta: AT GL_VERSION VERSION_NUMBER;


