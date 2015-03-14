package com.enremmeta.rtb.proto.adx;

import java.util.HashMap;
import java.util.Map;

/**
 * @see <a href=
 *      "https://storage.googleapis.com/adx-rtb-dictionaries/publisher-excludable-creative-attributes.txt">
 *      https://storage.googleapis.com/adx-rtb-dictionaries/publisher-excludable-creative-attributes
 *      .txt</a>
 *
 * @see <a href="https://storage.googleapis.com/adx-rtb-dictionaries/creative-status-codes.txt">
 *      https://storage.googleapis.com/adx-rtb-dictionaries/creative-status-codes.txt</a>
 *
 * @author Gregory Golberg ( <a href="mailto:grisha@alum.mit.edu">grisha@alum.mit.edu</a>)
 *         <p>
 *         </p>
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2015. All Rights
 *         Reserved. 
 *
 */
public interface AdXConstants {
    public static final int EXCLUDED_TAGGING_ISTAGGED = 7;
    public static final int EXCLUDED_COOKIETARGETING_ISCOOKIETARGETED = 8;
    public static final int EXCLUDED_USERINTERESTTARGETING_ISUSERINTERESTTARGETED = 9;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGUP = 13;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGDOWN = 14;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGLEFT = 15;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGRIGHT = 16;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGUPLEFT = 17;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGUPRIGHT = 18;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGDOWNLEFT = 19;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGDOWNRIGHT = 20;
    public static final int EXCLUDED_CREATIVETYPE_HTML = 21;
    public static final int EXCLUDED_CREATIVETYPE_VASTVIDEO = 22;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGUPORDOWN = 25;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGLEFTORRIGHT = 26;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGANYDIAGONAL = 27;
    public static final int EXCLUDED_EXPANDINGACTION_ROLLOVERTOEXPAND = 28;
    public static final int EXCLUDED_INSTREAMVASTVIDEOTYPE_VPAID_FLASH = 30;
    public static final int EXCLUDED_MRAIDTYPE_MRAID_1_0 = 32;
    public static final int EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYFLASH = 34;
    public static final int EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYHTML5 = 39;
    public static final int EXCLUDED_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYNONSSL = 48;
    public static final int EXCLUDED_EXPANDINGDIRECTION_EXPANDINGGDNAPI = 52;
    public static final int EXCLUDED_NATIVEELIGIBILITY_NATIVE_ELIGIBLE = 70;
    public static final int EXCLUDED_NATIVEELIGIBILITY_NATIVE_NOT_ELIGIBLE = 72;

    public static final int DECLARABLE_TAGGING_ISTAGGED = 7;
    public static final int DECLARABLE_COOKIETARGETING_ISCOOKIETARGETED = 8;
    public static final int DECLARABLE_USERINTERESTTARGETING_ISUSERINTERESTTARGETED = 9;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGNONE = 12;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGUP = 13;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGDOWN = 14;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGLEFT = 15;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGRIGHT = 16;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGUPLEFT = 17;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGUPRIGHT = 18;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGDOWNLEFT = 19;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGDOWNRIGHT = 20;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGUPORDOWN = 25;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGLEFTORRIGHT = 26;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGANYDIAGONAL = 27;
    public static final int DECLARABLE_EXPANDINGACTION_ROLLOVERTOEXPAND = 28;
    public static final int DECLARABLE_INSTREAMVASTVIDEOTYPE_VPAID_FLASH = 30;
    public static final int DECLARABLE_MRAIDTYPE_MRAID_1_0 = 32;
    public static final int DECLARABLE_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYFLASH = 34;
    public static final int DECLARABLE_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYHTML5 = 39;
    public static final int DECLARABLE_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYNONFLASH = 50;
    public static final int DECLARABLE_RICHMEDIACAPABILITYTYPE_RICHMEDIACAPABILITYSSL = 47;
    public static final int DECLARABLE_EXPANDINGDIRECTION_EXPANDINGGDNAPI = 52;
    public static final int DECLARABLE_NATIVEELIGIBILITY_NATIVE_ELIGIBLE = 70;
    public static final int DECLARABLE_NATIVEELIGIBILITY_NATIVE_NOT_ELIGIBLE = 70;

    public static final Map<Integer, String> CREATIVE_STATUS_CODE = new HashMap<Integer, String>()

    {
        {
            put(1, "Creative won the auction");
            put(2, "Internal error");
            put(3, "Unknown error (deprecated)");
            put(4, "Internal error: unknown status code");
            put(5, "Creative filtered due to landing page quality");
            put(6, "Creative filtered because malware was detected");
            put(7, "Creative has not yet been reviewed");
            put(8, "Unknown error (deprecated)");
            put(9, "Unknown error (deprecated)");
            put(10, "Creative was not approved");
            put(11, "Creative has not yet been categorized");
            put(12, "Creative filtered by publisher through the Publisher Controls ad review tool");
            put(13, "Creative filtered because the context of the inventory was incompatible with ad content, according to ad policy");
            put(14, "Creative filtered because of detected excluded_sensitive_category");
            put(15, "Creative filtered because one or more declared product categories were excluded in the bid request");
            put(16, "Creative filtered because one or more detected vendors are ineligible to serve ads on the Ad Exchange");
            put(17, "Creative filtered because one or more declared vendors were excluded in the bid request");
            put(18, "Creative filtered because one or more declared attributes were excluded in the bid request");
            put(19, "Creative filtered because one or more declared sensitive categories were excluded in the bid request");
            put(20, "Creative filtered because the required template field buyer_creative_id is missing");
            put(21, "Creative filtered because the required field buyer_creative_id is missing");
            put(22, "Creative filtered because no matching ad slot was found");
            put(23, "Creative filtered because the returned adgroup_id is invalid (not one of the matched adgroups)");
            put(24, "Creative filtered because the required adgroup_id field is missing");
            put(25, "Creative filtered because the declared width and height do not match the request");
            put(26, "Creative filtered because the required fields width or height are missing");
            put(27, "Creative filtered because it includes more than one of the following: html_snippet, video_url, snippet_template");
            put(28, "Creative filtered because click_through_url was not empty, should be empty when the TemplateParameter message is used");
            put(29, "Unknown error (deprecated)");
            put(30, "Unknown error (deprecated)");
            put(31, "Unknown error (deprecated)");
            put(32, "Creative filtered because one or more template_parameter fields were filled for a non-template ad");
            put(33, "Creative filtered because the required field html_snippet was empty");
            put(34, "Creative filtered because the required field video_url was empty");
            put(35, "Creative filtered because the required field snippet_template was empty");
            put(36, "Creative filtered because the required field click_through_url was empty");
            put(37, "Creative filtered because html_snippet and/or snippet_template fields were not empty, should be empty when video is requested");
            put(38, "Creative filtered because video_url field was not empty, should be empty unless video is requested");
            put(39, "Creative filtered because template did not have at least two instances of parameter_value");
            put(40, "Creative filtered because template has too many instances of parameter_value");
            put(41, "Creative filtered because buyer_creative_id in ad message was not empty, should only appear in TemplateParameter message for template ads");
            put(42, "Creative filtered because identical buyer_creative_id appears in more than one template parameter");
            put(43, "Creative filtered because the parameter with backup_index representing a backup ad appeared before the non-backup parameter");
            put(44, "Creative filtered because backup_index does not refer to a non-backup parameter");
            put(45, "Creative filtered because the required template field 'left' is missing");
            put(46, "Creative filtered because the required template field 'right' is missing");
            put(47, "Creative filtered because the required template field 'top' is missing");
            put(48, "Creative filtered because the required template field 'bottom' is missing");
            put(49, "Creative filtered because the template field 'left' is not greater than 0");
            put(50, "Creative filtered because the template field 'right' exceeds the maximum width of the requesting ad slot");
            put(51, "Creative filtered because the template field 'top' exceeds the maximum height of the requesting ad slot");
            put(52, "Creative filtered because the template field 'bottom' is not greater than 0");
            put(53, "Creative filtered because the bounding box is too narrow for template parameters");
            put(54, "Creative filtered because the height of bounding box is too low for template parameters");
            put(55, "Creative filtered because template parameter overlaps with a preceeding template parameter");
            put(56, "Creative filtered because the field click_through_url is too short, must be at least 11 characters");
            put(57, "Creative filtered because the field click_through_url could not be parsed");
            put(58, "Creative filtered because the field click_through_url has a domain consisting of all digits");
            put(59, "Creative filtered because the field video_url is too short, must be at least 11 characters");
            put(60, "Creative filtered because the field video_url could not be parsed");
            put(61, "Creative filtered because the field video_url has a domain consisting of all digits");
            put(62, "Creative filtered because the template field click_through_url was missing");
            put(63, "Creative filtered because an expandable vendor was declared, but expandable directions are missing from Ad.attribute");
            put(64, "Creative filtered because expandable directions were passed in Ad.attribute, but an expandable vendor declaration is missing");
            put(65, "Creative filtered because the template field buyer_creative_id exceeds 64 bytes");
            put(66, "Creative filtered because the field buyer_creative_id exceeds 64 bytes");
            put(67, "Creative filtered because max_cpm_micros is not greater than or equal to min_cpm_micros");
            put(68, "Creative filtered because max_cpm_micros is not greater than 0");
            put(69, "Status of creative remains unchecked since a previous template_parameter failed");
            put(70, "Unknown error (deprecated)");
            put(71, "Creative filtered due to publisher's URL exclusion settings");
            put(72, "Creative filtered due to publisher's advertiser exclusion settings");
            put(73, "Creative filtered due to publisher's targeting type settings");
            put(74, "Creative filtered because it has historically had a high error rate and/or very low view rate");
            put(75, "Creative filtered because the hosted creative had the wrong size or was not in a matching adgroup");
            put(76, "Creative filtered because the VAST XML contains errors or could not be fetched for verification");
            put(77, "Creative is awaiting review by the publisher");
            put(78, "Creative filtered because the declared creative size was not present in pre-targeting adgroup");
            put(79, "Creative was outbid");
            put(80, "Creative filtered because the bid price was below the minimum CPM threshold");
            put(81, "Creative was not allowed in Private Auction or Preferred Deal");
            put(82, "Creative filtered because the ad language is not allowed by the publisher");
            put(83, "The ad was part of a winning passback chain and will only show if preceding requests in the chain pass back");
            put(84, "Creative filtered because the publisher disallows ads from this agency");
            put(85, "Creative requires additional review");
            put(86, "Creative filtered because one or more undeclared restricted_category fields were detected");
            put(87, "Creative filtered because one or more declared restricted_category fields do not match the request");
            put(88, "Creative filtered because deal_id is not for a valid deal");
            put(89, "Interstitial ad size declared is either too big or too small to show on the device screen (it needs to cover at least 50% of screen width and 40% height)");
            put(90, "AdX VAST ad type does not match the type required in the video ad request");
            put(91, "Creative filtered because the duration of the video is too long");
            put(92, "Creative filtered because the duration of the video is too short");
            put(93, "Creative filtered because it is a skippable video ad for a non-skippable request");
            put(94, "Creative filtered because it is a non-skippable video ad for a skippable request");
            put(95, "Creative filtered because the skip offset is wrong (5 seconds is the correct value)");
            put(96, "Creative filtered because it lacks a MP4 video file in the VAST");
            put(97, "Creative filtered because it lacks a WebM video file in the VAST");
            put(98, "Creative filtered because it lacks a Flash video file in the VAST");
            put(99, "Creative filtered because it lacks a video file format in the VAST file all ads should have a Flash, MP4, and WebM file");
            put(100, "Creative filtered because one or more of the required template fields left, right, top, or bottom is missing");
            put(101, "Creative filtered because template fields left or top are not greater than 0, or right or bottom exceed the ad slot dimensions");
            put(102, "Creative filtered because the bidder account is in testing mode");
            put(103, "Native ad specified for an adslot that does not support it");
            put(104, "native_ad.image.url field value is an invalid URL");
            put(105, "native_ad.logo.url field value is an invalid URL");
            put(106, "native_ad.app_icon.url field value is an invalid URL");
            put(107, "Required elements specified in the bit field bid_request.adslot.native_ad_template.required_fields are missing");
            put(108, "native_ad.impression_tracking_url field value is too short to be a valid URL");
            put(109, "native_ad.impression_tracking_url field value cannot be parsed as a valid URL");
            put(110, "native_ad.impression_tracking_url field value has a domain that is all digits");
            put(111, "native_ad.click_tracking_url field value is too short to be a valid URL");
            put(112, "native_ad.click_tracking_url field value cannot be parsed as a valid URL");
            put(113, "native_ad.click_tracking_url field value has a domain that is all digits");
            put(114, "Creative filtered because deal_id is invalid");
            put(115, "ADX_AD_SOURCE_TYPE_MANAGED_TAG does not support deals.");
            put(116, "Creative filtered because deal's buyer_network_id is invalid");
            put(117, "Creative filtered because deal's advertiser is invalid");
            put(118, "Creative filtered because a deal id is required");
            put(119, "Creative filtered by publisher's exclusion rules");
            put(120, "Creative filtered because the language could not be detected and the publisher has language restrictions.");
            put(121, "Creative filtered because the billing entity it is attributed to is out of budget");
            put(122, "Creative filtered due to frequency caps");
            put(123, "Creative filtered because it is not allowed to serve on the Android platform");
            put(124, "Creative filtered because it is not allowed to serve on the iOS platform");
            put(125, "impression_tracking_url field value is too short to be a valid URL");
            put(126, "impression_tracking_url field value cannot be parsed as a valid URL");
            put(127, "impression_tracking_url field value has a domain that is all digits");
            put(128, "impression_tracking_url not supported for non mobile-app html ads");
            put(129, "Creative filtered because it is ineligible or unapproved for serving to a Chinese user or property");
            put(130, "Creative filtered because it is ineligible or unapproved for serving to a Russian user or property");
            put(131, "Creative has not yet been reviewed and the account has too many active snippets");
            put(132, "native_ad.image.width or native_ad.image.height has an invalid value");
            put(133, "native_ad.logo.width or native_ad.logo.height has an invalid value");
            put(134, "native_ad.app_icon.width or native_ad.app_icon.height has an invalid value");
            put(135, "native_ad.store field value is an invalid URL");
            put(136, "native_ad.star_rating field value is invalid");
            put(137, "Creative filtered because ad.width/height is not allowed for the chosen deal");
            put(138, "Creative filtered because max_cpm_micros is too large");
            put(139, "Creative filtered because bidder used an unsupported OpenRTB feature");
        }
    };
}
