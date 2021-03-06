package com.khizhny.smsbanking;

import android.content.ContentValues;
import android.util.Log;
import static com.khizhny.smsbanking.MyApplication.LOG;

public class SubRule implements java.io.Serializable {

	private final static long serialVersionUID = 1; // Is used to indicate class version during Import/Export
	private int id;
	private int ruleId;
	private int distanceToLeftPhrase;
	private int distanceToRightPhrase;
	private String leftPhrase;
	private String rightPhrase;
	private String constantValue;
	private Transaction.Parameters extractedParameter;
	private Method extractionMethod;
	private int decimalSeparator;
	private String separator;
	private int trimLeft;
	private int trimRight;
	private boolean negate;

	public enum Method {
		WORD_AFTER_PHRASE,
		WORD_BEFORE_PHRASE,
		WORDS_BETWEEN_PHRASES,
		USE_CONSTANT
	}

	/**
	 * Default SubRule constructor.
	 * @param ruleId ID of parent Rule.
	 */
	SubRule(int ruleId) {
		this.id = -1;
		this.ruleId = ruleId;
		this.distanceToLeftPhrase = 1;
		this.distanceToRightPhrase = 1;
		this.leftPhrase="";
		this.rightPhrase="";
		this.constantValue="";
		this.extractedParameter = Transaction.Parameters.ACCOUNT_STATE_AFTER;
		this.extractionMethod = Method.WORD_AFTER_PHRASE;
		this.decimalSeparator = 0;
		this.separator = ".";
		this.trimLeft = 0;
		this.trimRight = 0;
		this.negate = false;
	}

	/**
	 * Constructor for cloning Object
	 * @param origin Original object that is copied.
	 * @param ruleId Id of the new_rule to which new subrule will be attached.
	 */
	SubRule(SubRule origin, int ruleId) {
		this.id = -1;
		this.ruleId = ruleId;
		this.distanceToLeftPhrase = origin.distanceToLeftPhrase;
		this.distanceToRightPhrase = origin.distanceToRightPhrase;
		this.leftPhrase=origin.leftPhrase;
		this.rightPhrase=origin.rightPhrase;
		this.constantValue=origin.constantValue;
		this.extractedParameter = origin.extractedParameter;
		this.extractionMethod = origin.extractionMethod;
		this.decimalSeparator = origin.decimalSeparator;
		this.separator = origin.separator;
		this.trimLeft = origin.trimLeft;
		this.trimRight = origin.trimRight;
		this.negate = origin.negate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDistanceToLeftPhrase() {
		return distanceToLeftPhrase;
	}

	public void setDistanceToLeftPhrase(int distanceToLeftPhrase) {
		this.distanceToLeftPhrase = distanceToLeftPhrase;
	}

	public int getDistanceToRightPhrase() {
		return distanceToRightPhrase;
	}

	public void setDistanceToRightPhrase(int distanceToRightPhrase) {
		this.distanceToRightPhrase = distanceToRightPhrase;
	}

	public String getLeftPhrase() {
		return leftPhrase;
	}

	public void setLeftPhrase(String leftPhrase) {
		this.leftPhrase = leftPhrase;
	}

	public String getRightPhrase() {
		return rightPhrase;
	}

	public void setRightPhrase(String rightPhrase) {
		this.rightPhrase = rightPhrase;
	}

	public int getExtractionMethodInt() {
		return extractionMethod.ordinal();
	}

	public Method getExtractionMethod() {
		return extractionMethod;
	}

	public void setExtractionMethod(int extractionMethod) {
		this.extractionMethod = Method.values()[extractionMethod];
	}

	public String getConstantValue() {
		return constantValue;
	}

	public void setConstantValue(String constantValue) {
		this.constantValue = constantValue;
	}

	public int getExtractedParameterInt() {
		return extractedParameter.ordinal();
	}

	public Transaction.Parameters getExtractedParameter() {
		return extractedParameter;
	}

	public void setExtractedParameter(int extractedParameter) {
		this.extractedParameter = Transaction.Parameters.values()[extractedParameter];
	}

	public int getDecimalSeparator() {
        return decimalSeparator;
    }

	public void setDecimalSeparator(int decimalSeparator) {
		if (decimalSeparator == 0) {
            this.separator = ".";
		} else {
            this.separator = ",";
		}
		this.decimalSeparator = decimalSeparator;
	}

    public void setDecimalSeparator(String decimalSeparator) {
        if (decimalSeparator.equals(".")) {
            this.decimalSeparator =0;
        } else {
            this.decimalSeparator =1;
        }
        this.separator= decimalSeparator;
    }

	public int getTrimLeft() {
		return trimLeft;
	}

	public void setTrimLeft(int trimLeft) {
		this.trimLeft = trimLeft;
	}

	public int getTrimRight() {
		return trimRight;
	}

	public void setTrimRight(int trimRight) {
		this.trimRight = trimRight;
	}

	/**
	 * These Function will try to apply rule to SMS message.
	 * If returnedType=0 result will be Numeric.
	 * If returnedType=1 result will be Alphabetical.
	 * If returnedType>1 result will be String AS-IS. Unchanged after extraction.
	 *
	 * @param msg          SMS message text
	 * @param returnedType Zero for decimal, One for Alphabetical, Other for unchanged string.
	 * @return Parameter value
	 */
	public String applySubRule(String msg, int returnedType) {
		msg = "<BEGIN> " + msg + " <END>";
		String temp = "";
		try {
            String[] arr;
            int wordsCount;
			switch (extractionMethod) {
				case WORD_AFTER_PHRASE:
					temp = msg.split(String.format("\\Q%s\\E", leftPhrase))[1].split(" ")[distanceToLeftPhrase];
					break;
				case WORD_BEFORE_PHRASE:
					temp = msg.split(String.format("\\Q%s\\E", rightPhrase))[0].trim();
					arr = temp.split(" ");
					wordsCount = arr.length;
					temp = (arr[wordsCount - distanceToRightPhrase]);
					break;
				case WORDS_BETWEEN_PHRASES:
					// temp will store all words between phrases "leftPhrase" and "rightPhrase"
                    temp = msg.split(String.format("\\Q%s\\E", leftPhrase))[1].split(String.format("\\Q%s\\E", rightPhrase))[0].trim();
                    // Now we will just keep phrase starting from N-th word from the left till M-th word wrom the right.
                    // where N - distanceToLeftPhrase
                    // M - distanceToRightPhrase
                    arr = temp.split(" ");
                    temp="";
                    for (int j=distanceToLeftPhrase-1; j<arr.length-distanceToRightPhrase+1;j++) {
                        if (j > distanceToLeftPhrase - 1) temp = temp + " ";
                        temp = temp + arr[j];
                    }
                    break;
				case USE_CONSTANT:
					temp = constantValue;
					break;
			}
		} catch (Exception e) {
			return "";
		}
		// trimming characters if needed
		if (trimRight > 0 || trimLeft > 0) {
			int temp_len = temp.length();
			try {
				temp = temp.substring(trimLeft, temp_len - trimRight);
			} catch (Exception e) {
				return "";
			}
		}

		switch (returnedType) {
			case 0: // return only Numbers
				try {
					// changing sign if needed
					if (negate) {
						if (!temp.contains("-")) {
							temp = "-" + temp;
						} else {
							temp = temp.replace("-", "");
						}
					}
					temp=temp.replaceAll("[^0-9" + separator + "-]", "");
				} catch (Exception e) {
                    temp="0";
				}
                break;
			case 1: // return only text
                temp=temp.replaceAll("[^A-Za-z]", "");
                break;
			default: // return as-is.
                break;
		}
        return temp;
	}

	/**
	 * This function will apply extraction subrule on SMS message and save extracted value to Transaction object
	 *
	 * @param msg         SMS Message
	 * @param transaction Transaction
	 * @return False if subrule cannot be applied
	 */
	public Boolean applySubRule(String msg, Transaction transaction) {
		switch (extractedParameter) {
			case ACCOUNT_STATE_BEFORE: //Account state before transaction
				transaction.setStateBefore(applySubRule(msg, 0));
				return true;
			case ACCOUNT_STATE_AFTER: //Account state after transaction
				transaction.setStateAfter(applySubRule(msg, 0));
				return true;
			case ACCOUNT_DIFFERENCE: //Account difference
				transaction.setDifference(applySubRule(msg, 0));
				return true;
			case COMMISSION: //Transaction commission
				transaction.setComission(applySubRule(msg, 0));
				return true;
			case CURRENCY: //Transaction currency
				transaction.setTransactionCurrency(applySubRule(msg, 1)); // return only text)
				return true;
			case EXTRA_1:
				transaction.setExtraParam1(applySubRule(msg, 2));
				return true;
			case EXTRA_2:
				transaction.setExtraParam2(applySubRule(msg, 2));
				return true;
			case EXTRA_3:
				transaction.setExtraParam3(applySubRule(msg, 2));
				return true;
			case EXTRA_4:
				transaction.setExtraParam4(applySubRule(msg, 2));
				return true;
			default:
				Log.d(LOG, "Unexpected parameter number " + extractedParameter + " in Rule ID=" + ruleId);
				return false;
		}

	}

	public boolean isNegate() {
		return negate;
	}

	public int getNegateInt() {
		return negate ? -1 : 0;
	}

	public void setNegate(boolean negate) {
		this.negate = negate;
	}

	/**
	 * Changes Rule Id to make possible Template copy to myBanks
	 * @param ruleId Rule ID in Db.
	 */
	public void changeRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

	/**
	 * @return Content values used for database insert or update function.
	 */
	public ContentValues getContentValues() {
		ContentValues v = new ContentValues();
		if (id >= 1) v.put("_id", id);
		v.put("rule_id", ruleId);
		v.put("left_phrase", leftPhrase);
		v.put("right_phrase", rightPhrase);
		v.put("distance_to_left_phrase", distanceToLeftPhrase);
		v.put("distance_to_right_phrase", distanceToRightPhrase);
		v.put("constant_value", constantValue);
		v.put("extracted_parameter", getExtractedParameterInt());
		v.put("extraction_method", getExtractionMethodInt());
		v.put("decimal_separator", decimalSeparator);
		v.put("trim_left", trimLeft);
		v.put("trim_right", trimRight);
		v.put("negate", getNegateInt());
		return v;
	}
}