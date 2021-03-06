/**
 * @module br/validation/NumericValidator
 */

var brCore = require("br/Core");
var Validator = require("br/validation/Validator");

/**
 * @class
 * @alias module:br/validation/NumericValidator
 * @implements module:br/validation/Validator
 */
var NumericValidator = function(sFailureMessage)
{
	this.sMessage = sFailureMessage;
	/*
	* The first boolean part of the Regex allows for:
	* 123, .123, 1.23,
	* but will not match "123." hence [\d]+\.
	 */
	this.m_oRegex = new RegExp(/^[-+]?(([\d]*\.?[\d]+)|([\d]+\.))$/);
};

brCore.implement(NumericValidator, Validator);

NumericValidator.prototype.validate = function(vValue, mAttributes, oValidationResult)
{
	var bIsValid = false;
	if((typeof vValue === 'string' || typeof vValue === 'number') && this.m_oRegex.test(vValue))
	{
		bIsValid = true;
	}

	var oTranslator = require("br/I18n").getTranslator();
	var sFailureMessage = oTranslator.tokenExists(this.sMessage) ? oTranslator.getMessage(this.sMessage,{sInput:vValue}) : this.sMessage;

	oValidationResult.setResult(bIsValid, sFailureMessage);
};

module.exports = NumericValidator;
