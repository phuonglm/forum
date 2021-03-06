/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.answer.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.answer.webui.FAQUtils;
import org.exoplatform.answer.webui.UIAnswersContainer;
import org.exoplatform.answer.webui.UIAnswersPortlet;
import org.exoplatform.answer.webui.UICategories;
import org.exoplatform.answer.webui.UIQuestions;
import org.exoplatform.answer.webui.ValidatorDataInput;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 
 * Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/answer/webui/popup/UISettingForm.gtmpl", 
    events = {
        @EventConfig(listeners = UISettingForm.SaveActionListener.class), 
        @EventConfig(listeners = UISettingForm.UserWatchManagerActionListener.class), 
        @EventConfig(listeners = UISettingForm.ChildTabChangeActionListener.class), 
        @EventConfig(listeners = UISettingForm.ResetMailContentActionListener.class), 
        @EventConfig(listeners = UISettingForm.SelectCategoryForumActionListener.class), 
        @EventConfig(listeners = UISettingForm.ChangeAvatarActionListener.class), 
        @EventConfig(listeners = UISettingForm.SetDefaultAvatarActionListener.class, confirm = "UISettingForm.msg.SetDeFaultAvatar", phase = Phase.DECODE), 
        @EventConfig(listeners = UISettingForm.CancelActionListener.class) 
    }
)
public class UISettingForm extends BaseUIForm implements UIPopupComponent {
  public final String         DISPLAY_TAB                      = "DisplayTab";

  public final String         SET_DEFAULT_EMAIL_TAB            = "DefaultEmail";

  public final String         SET_DEFAULT_ADDNEW_QUESTION_TAB  = "AddNewQuestionTab";

  public final String         SET_DEFAULT_EDIT_QUESTION_TAB    = "EditQuestionTab";

  public final String         SET_EMAIL_MOVE_QUESTION_TAB      = "EmailMoveQuestionTab";

  public final String         CATEGORY_SCOPING                 = "CategoryScoping";

  public final String         ITEM_VOTE                        = "vote";

  private final String        DISPLAY_MODE                     = "display-mode".intern();

  public static final String  ORDER_BY                         = "order-by".intern();

  public static final String  ORDER_TYPE                       = "order-type".intern();

  private static final String ENABLE_VOTE_COMMNET              = "enableVotComment";

  private static final String ENABLE_ANONYMOUS_SUBMIT_QUESTION = "enableAnonymousSubmitQuestion";

  public static final String  ITEM_CREATE_DATE                 = "created".intern();

  public static final String  ITEM_ALPHABET                    = "alphabet".intern();

  public static final String  ASC                              = "asc".intern();

  public static final String  DESC                             = "desc".intern();

  private static final String ENABLE_RSS                       = "enableRSS";

  private static final String ENABLE_VIEW_AVATAR               = "enableViewAvatar";

  private static final String EMAIL_DEFAULT_ADD_QUESTION       = "EmailAddNewQuestion";

  private static final String EMAIL_DEFAULT_EDIT_QUESTION      = "EmailEditQuestion";

  private static final String EMAIL_MOVE_QUESTION              = "EmailMoveQuestion";

  private static final String DISCUSSION_TAB                   = "Discussion";

  private static final String FIELD_CATEGORY_PATH_INPUT        = "CategoryPath";

  private static final String ENABLE_DISCUSSION                = "EnableDiscuss";

  private static final String POST_QUESTION_IN_ROOT_CATEGORY   = "isPostQuestionInRootCategory";

  private FAQSetting          faqSetting_                      = new FAQSetting();

  private boolean             isEditPortlet_                   = false;

  private List<String>        idForumName                      = new ArrayList<String>();

  protected boolean           isResetMail                      = false;

  protected int               indexOfTab                       = 0;

  private String              avatarUrl;

  private String              tabSelected                      = DISPLAY_TAB;

  private List<Cate>          listCate                         = new ArrayList<Cate>();

  private FAQService          faqService_;

  public UISettingForm() throws Exception {
    faqService_ = FAQUtils.getFAQService();
    isEditPortlet_ = false;
  }

  public void setIsEditPortlet(boolean isEditPortLet) {
    this.isEditPortlet_ = isEditPortLet;
    if (isEditPortLet) {
      FAQUtils.getPorletPreference(faqSetting_);
    }
  }

  public void setPathCatygory(List<String> idForumName) {
    this.idForumName = idForumName;
    ((UIFormInputWithActions) getChildById(DISCUSSION_TAB)).getUIStringInput(FIELD_CATEGORY_PATH_INPUT).setValue(idForumName.get(1));
  }

  private void setListCate() throws Exception {
    this.listCate.addAll(FAQUtils.getFAQService().listingCategoryTree());
  }

  public void init() throws Exception {
    if (isEditPortlet_) {
      setListCate();

      UIFormInputWithActions DisplayTab = new UIFormInputWithActions(DISPLAY_TAB);
      UIFormInputWithActions EmailTab = new UIFormInputWithActions(SET_DEFAULT_EMAIL_TAB);
      UIFormInputWithActions EmailAddNewQuestion = new UIFormInputWithActions(SET_DEFAULT_ADDNEW_QUESTION_TAB);
      UIFormInputWithActions EmailEditQuestion = new UIFormInputWithActions(SET_DEFAULT_EDIT_QUESTION_TAB);
      UIFormInputWithActions EmailMoveQuestion = new UIFormInputWithActions(SET_EMAIL_MOVE_QUESTION_TAB);
      UIFormInputWithActions Discussion = new UIFormInputWithActions(DISCUSSION_TAB);
      UIFormInputWithActions CategoryScoping = new UIFormInputWithActions(CATEGORY_SCOPING);

      List<SelectItemOption<String>> displayMode = new ArrayList<SelectItemOption<String>>();
      displayMode.add(new SelectItemOption<String>(FAQSetting.DISPLAY_APPROVED, FAQSetting.DISPLAY_APPROVED));
      displayMode.add(new SelectItemOption<String>(FAQSetting.DISPLAY_BOTH, FAQSetting.DISPLAY_BOTH));

      List<SelectItemOption<String>> orderBy = new ArrayList<SelectItemOption<String>>();
      orderBy.add(new SelectItemOption<String>(ITEM_CREATE_DATE, FAQSetting.DISPLAY_TYPE_POSTDATE));
      orderBy.add(new SelectItemOption<String>(ITEM_ALPHABET + "/Index", FAQSetting.DISPLAY_TYPE_ALPHABET + "/Index"));

      List<SelectItemOption<String>> orderType = new ArrayList<SelectItemOption<String>>();
      orderType.add(new SelectItemOption<String>(ASC, FAQSetting.ORDERBY_TYPE_ASC));
      orderType.add(new SelectItemOption<String>(DESC, FAQSetting.ORDERBY_TYPE_DESC));

      FAQUtils.getEmailSetting(faqSetting_, true, false);
      UIFormWYSIWYGInput emailDefaultAdd = new UIFormWYSIWYGInput(EMAIL_DEFAULT_ADD_QUESTION, EMAIL_DEFAULT_ADD_QUESTION, "");
      emailDefaultAdd.setFCKConfig(WebUIUtils.getFCKConfig());
      emailDefaultAdd.setToolBarName("Basic");
      emailDefaultAdd.setValue(faqSetting_.getEmailSettingContent());
      EmailAddNewQuestion.addUIFormInput(emailDefaultAdd);

      FAQUtils.getEmailSetting(faqSetting_, false, false);
      UIFormWYSIWYGInput emailDefaultEdit = new UIFormWYSIWYGInput(EMAIL_DEFAULT_EDIT_QUESTION, EMAIL_DEFAULT_EDIT_QUESTION, "");
      emailDefaultEdit.setFCKConfig(WebUIUtils.getFCKConfig());
      emailDefaultEdit.setToolBarName("Basic");
      emailDefaultEdit.setValue(faqSetting_.getEmailSettingContent());
      EmailEditQuestion.addUIFormInput(emailDefaultEdit);

      String defEmailMove = faqSetting_.getEmailMoveQuestion();
      if (defEmailMove == null || defEmailMove.trim().length() <= 10) {
        defEmailMove = FAQUtils.getEmailMoveQuestion(faqSetting_);
      }

      UIFormWYSIWYGInput emailDefaultMove = new UIFormWYSIWYGInput(EMAIL_MOVE_QUESTION, EMAIL_MOVE_QUESTION, "");
      emailDefaultMove.setFCKConfig(WebUIUtils.getFCKConfig());
      emailDefaultMove.setToolBarName("Basic");
      emailDefaultMove.setValue(defEmailMove);
      EmailMoveQuestion.addUIFormInput(emailDefaultMove);
      EmailTab.addChild(EmailAddNewQuestion);
      EmailTab.addChild(EmailEditQuestion);
      EmailTab.addChild(EmailMoveQuestion);

      DisplayTab.addUIFormInput((new UIFormSelectBox(DISPLAY_MODE, DISPLAY_MODE, displayMode)).setValue(faqSetting_.getDisplayMode()));
      DisplayTab.addUIFormInput((new UIFormSelectBox(ORDER_BY, ORDER_BY, orderBy)).setValue(String.valueOf(faqSetting_.getOrderBy())));
      DisplayTab.addUIFormInput((new UIFormSelectBox(ORDER_TYPE, ORDER_TYPE, orderType)).setValue(String.valueOf(faqSetting_.getOrderType())));
      DisplayTab.addUIFormInput((new UICheckBoxInput(ENABLE_VOTE_COMMNET, ENABLE_VOTE_COMMNET, false)).setChecked(faqSetting_.isEnanbleVotesAndComments()));
      DisplayTab.addUIFormInput((new UICheckBoxInput(ENABLE_ANONYMOUS_SUBMIT_QUESTION, ENABLE_ANONYMOUS_SUBMIT_QUESTION, false)).setChecked(faqSetting_.isEnableAnonymousSubmitQuestion()));
      DisplayTab.addUIFormInput((new UICheckBoxInput(ENABLE_RSS, ENABLE_RSS, false)).setChecked(faqSetting_.isEnableAutomaticRSS()));
      DisplayTab.addUIFormInput((new UICheckBoxInput(ENABLE_VIEW_AVATAR, ENABLE_VIEW_AVATAR, false)).setChecked(faqSetting_.isEnableViewAvatar()));
      UICheckBoxInput isPostQuestionInRootCategory = new UICheckBoxInput(POST_QUESTION_IN_ROOT_CATEGORY, POST_QUESTION_IN_ROOT_CATEGORY, true);
      isPostQuestionInRootCategory.setChecked(faqSetting_.isPostQuestionInRootCategory());
      DisplayTab.addUIFormInput(isPostQuestionInRootCategory);
      UICheckBoxInput enableDiscus = new UICheckBoxInput(ENABLE_DISCUSSION, ENABLE_DISCUSSION, false);
      enableDiscus.setChecked(faqSetting_.getIsDiscussForum());
      Discussion.addUIFormInput(enableDiscus);
      UIFormStringInput categoryPath = new UIFormStringInput(FIELD_CATEGORY_PATH_INPUT, FIELD_CATEGORY_PATH_INPUT, null);
      String pathCate = faqSetting_.getIdNameCategoryForum();
      idForumName.clear();
      if (pathCate.indexOf(";") > 0) {
        this.idForumName.add(pathCate.substring(0, pathCate.indexOf(";")));
        this.idForumName.add(pathCate.substring(pathCate.indexOf(";") + 1));
      } else {
        this.idForumName.add("");
        this.idForumName.add("");
      }
      categoryPath.setValue(idForumName.get(1));
      categoryPath.setReadOnly(true);
      Discussion.addUIFormInput(categoryPath);
      List<ActionData> actionData = new ArrayList<ActionData>();
      ActionData ad;
      ad = new ActionData();
      ad.setActionListener("SelectCategoryForum");
      ad.setActionName("SelectCategoryForum");
      ad.setActionType(ActionData.TYPE_ICON);
      ad.setCssIconClass("AddIcon16x16");
      actionData.add(ad);
      Discussion.setActionField(FIELD_CATEGORY_PATH_INPUT, actionData);

      UICheckBoxInput checkBoxInput = null;
      for (Cate cate : listCate) {
        checkBoxInput = new UICheckBoxInput(cate.getCategory().getId(), cate.getCategory().getId(), false);
        checkBoxInput.setChecked(cate.getCategory().isView());
        CategoryScoping.addChild(checkBoxInput);
      }

      this.addChild(DisplayTab);
      this.addChild(EmailTab);
      this.addChild(Discussion);
      this.addChild(CategoryScoping);

      DisplayTab.setRendered(true);
      EmailAddNewQuestion.setRendered(true);
      EmailEditQuestion.setRendered(true);
      EmailTab.setRendered(true);
    } else {

      List<SelectItemOption<String>> orderBy = new ArrayList<SelectItemOption<String>>();
      orderBy.add(new SelectItemOption<String>(ITEM_CREATE_DATE, FAQSetting.DISPLAY_TYPE_POSTDATE));
      orderBy.add(new SelectItemOption<String>(ITEM_ALPHABET + "/Index", FAQSetting.DISPLAY_TYPE_ALPHABET + "/Index"));
      addUIFormInput((new UIFormSelectBox(ORDER_BY, ORDER_BY, orderBy)).setValue(String.valueOf(faqSetting_.getOrderBy())));

      List<SelectItemOption<String>> orderType = new ArrayList<SelectItemOption<String>>();
      orderType.add(new SelectItemOption<String>(ASC, FAQSetting.ORDERBY_TYPE_ASC));
      orderType.add(new SelectItemOption<String>(DESC, FAQSetting.ORDERBY_TYPE_DESC));
      addUIFormInput((new UIFormSelectBox(ORDER_TYPE, ORDER_TYPE, orderType)).setValue(String.valueOf(faqSetting_.getOrderType())));

      addUIFormInput((new UICheckBoxInput(ITEM_VOTE, ITEM_VOTE, false)).setChecked(faqSetting_.isSortQuestionByVote()));

      setAvatarUrl(FAQUtils.getUserAvatar(FAQUtils.getCurrentUser()));
    }
  }

  boolean isDefaultAvatar() {
    return avatarUrl != null && avatarUrl.indexOf(Utils.DEFAULT_AVATAR_URL) >= 0;
  }

  /**
   * @return the avatarUrl
   */
  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String url) {
    this.avatarUrl = url;
  }

  public FAQSetting getFaqSetting() {
    return faqSetting_;
  }

  public void setFaqSetting(FAQSetting faqSetting) {
    this.faqSetting_ = faqSetting;
  }

  public String[] getActions() {
    return new String[] { "Save", "Cancel" };
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected String getSelectedTab() {
    return tabSelected;
  }

  static public class SaveActionListener extends EventListener<UISettingForm> {
    public void execute(Event<UISettingForm> event) throws Exception {
      UISettingForm settingForm = event.getSource();
      UIAnswersPortlet uiPortlet = settingForm.getAncestorOfType(UIAnswersPortlet.class);
      FAQSetting faqSetting = settingForm.faqSetting_;
      if (settingForm.isEditPortlet_) {
        UIFormInputWithActions inputWithActions = settingForm.getChildById(settingForm.CATEGORY_SCOPING);
        List<String> listCateIds = new ArrayList<String>();
        UICheckBoxInput checkBoxInput = null;
        int position = 1;
        boolean isView = true;
        for (int i = 0; i < settingForm.listCate.size(); i++) {
          checkBoxInput = inputWithActions.getChildById(settingForm.listCate.get(i).getCategory().getId());
          if (settingForm.listCate.get(i).getDeft() <= position || (!checkBoxInput.isChecked() && isView)) {
            isView = settingForm.listCate.get(i).getCategory().isView();
            position = settingForm.listCate.get(i).getDeft();
          }
          if ((settingForm.listCate.get(i).getDeft() > position) && !isView)
            checkBoxInput.setChecked(false);
          if ((checkBoxInput.isChecked() && !settingForm.listCate.get(i).getCategory().isView()) || (!checkBoxInput.isChecked() && settingForm.listCate.get(i).getCategory().isView())) {
            listCateIds.add(settingForm.listCate.get(i).getCategory().getPath());
            settingForm.listCate.get(i).getCategory().setView(checkBoxInput.isChecked());
          }
        }
        if (listCateIds != null && listCateIds.size() > 0)
          settingForm.faqService_.changeStatusCategoryView(listCateIds);

        inputWithActions = settingForm.getChildById(settingForm.DISPLAY_TAB);
        faqSetting.setDisplayMode(((UIFormSelectBox) inputWithActions.getChildById(settingForm.DISPLAY_MODE)).getValue());
        faqSetting.setOrderBy(String.valueOf(((UIFormSelectBox) inputWithActions.getChildById(ORDER_BY)).getValue()));
        faqSetting.setOrderType(String.valueOf(((UIFormSelectBox) inputWithActions.getChildById(ORDER_TYPE)).getValue()));
        faqSetting.setEnanbleVotesAndComments(inputWithActions.getUICheckBoxInput(ENABLE_VOTE_COMMNET).isChecked());
        faqSetting.setEnableAnonymousSubmitQuestion(inputWithActions.getUICheckBoxInput(ENABLE_ANONYMOUS_SUBMIT_QUESTION).isChecked());
        faqSetting.setEnableAutomaticRSS(inputWithActions.getUICheckBoxInput(ENABLE_RSS).isChecked());
        faqSetting.setEnableViewAvatar(inputWithActions.getUICheckBoxInput(ENABLE_VIEW_AVATAR).isChecked());
        faqSetting.setPostQuestionInRootCategory(inputWithActions.getUICheckBoxInput(POST_QUESTION_IN_ROOT_CATEGORY).isChecked());

        UIFormInputWithActions emailTab = settingForm.getChildById(settingForm.SET_DEFAULT_EMAIL_TAB);
        String defaultAddnewQuestion = ((UIFormWYSIWYGInput) ((UIFormInputWithActions) emailTab.getChildById(settingForm.SET_DEFAULT_ADDNEW_QUESTION_TAB)).getChildById(EMAIL_DEFAULT_ADD_QUESTION)).getValue();
        String defaultEditQuestion = ((UIFormWYSIWYGInput) ((UIFormInputWithActions) emailTab.getChildById(settingForm.SET_DEFAULT_EDIT_QUESTION_TAB)).getChildById(EMAIL_DEFAULT_EDIT_QUESTION)).getValue();

        String emailMoveQuestion = ((UIFormWYSIWYGInput) ((UIFormInputWithActions) emailTab.getChildById(settingForm.SET_EMAIL_MOVE_QUESTION_TAB)).getChildById(EMAIL_MOVE_QUESTION)).getValue();

        if (defaultAddnewQuestion == null || !ValidatorDataInput.fckContentIsNotEmpty(defaultAddnewQuestion))
          defaultAddnewQuestion = " ";
        if (defaultEditQuestion == null || !ValidatorDataInput.fckContentIsNotEmpty(defaultEditQuestion))
          defaultEditQuestion = " ";
        UIFormInputWithActions Discussion = settingForm.getChildById(DISCUSSION_TAB);
        boolean isDiscus = (Boolean) Discussion.getUICheckBoxInput(ENABLE_DISCUSSION).getValue();
        if (isDiscus) {
          String value = Discussion.getUIStringInput(FIELD_CATEGORY_PATH_INPUT).getValue();
          if (!settingForm.idForumName.isEmpty() && !FAQUtils.isFieldEmpty(value)) {
            faqSetting.setIdNameCategoryForum(settingForm.idForumName.get(0) + ";" + settingForm.idForumName.get(1));
          } else {
            event.getRequestContext()
                 .getUIApplication()
                 .addMessage(new ApplicationMessage("UISettingForm.msg.pathCategory-empty", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
            return;
          }
        } else {
          faqSetting.setIdNameCategoryForum("");
        }
        faqSetting.setIsDiscussForum(isDiscus);
        faqSetting.setEmailMoveQuestion(emailMoveQuestion);
        FAQUtils.savePortletPreference(faqSetting, defaultAddnewQuestion.replaceAll("&amp;", "&"), defaultEditQuestion.replaceAll("&amp;", "&"));
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UISettingForm.msg.update-successful",
                                                                                       null,
                                                                                       ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
      } else {
        faqSetting.setOrderBy(String.valueOf(settingForm.getUIFormSelectBox(ORDER_BY).getValue()));
        faqSetting.setOrderType(String.valueOf(settingForm.getUIFormSelectBox(ORDER_TYPE).getValue()));
        faqSetting.setSortQuestionByVote(settingForm.getUICheckBoxInput(settingForm.ITEM_VOTE).isChecked());
        settingForm.faqService_.saveFAQSetting(faqSetting, FAQUtils.getCurrentUser());
        UIPopupAction uiPopupAction = settingForm.getAncestorOfType(UIPopupAction.class);
        uiPopupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
        UIQuestions questions = uiPortlet.findFirstComponentOfType(UIQuestions.class);
        UICategories categories = uiPortlet.findFirstComponentOfType(UICategories.class);
        categories.resetListCate();
        questions.setFAQSetting(faqSetting);
        questions.setListObject();
        questions.updateCurrentQuestionList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
      }
    }
  }

  static public class UserWatchManagerActionListener extends BaseEventListener<UISettingForm> {
    public void onEvent(Event<UISettingForm> event, UISettingForm settingForm, String objectId) throws Exception {
      UIPopupContainer watchContainer = settingForm.getAncestorOfType(UIPopupContainer.class);
      UIUserWatchManager watchForm = openPopup(watchContainer, UIUserWatchManager.class, 600, 0);
      watchForm.setFAQSetting(settingForm.faqSetting_);
    }
  }

  static public class ChangeAvatarActionListener extends BaseEventListener<UISettingForm> {
    public void onEvent(Event<UISettingForm> event, UISettingForm settingForm, String objectId) throws Exception {
      UIPopupContainer watchContainer = settingForm.getAncestorOfType(UIPopupContainer.class);
      UIAttachmentForm attachMentForm = openPopup(watchContainer, UIAttachmentForm.class, 550, 0);
      attachMentForm.setIsChangeAvatar(true);
      attachMentForm.setNumberUpload(1);
    }
  }

  static public class SetDefaultAvatarActionListener extends EventListener<UISettingForm> {
    public void execute(Event<UISettingForm> event) throws Exception {
      UISettingForm settingForm = event.getSource();
      settingForm.faqService_.setDefaultAvatar(FAQUtils.getCurrentUser());
      settingForm.setAvatarUrl(Utils.DEFAULT_AVATAR_URL);
      event.getRequestContext().addUIComponentToUpdateByAjax(settingForm.getParent());
    }
  }

  static public class ResetMailContentActionListener extends EventListener<UISettingForm> {
    public void execute(Event<UISettingForm> event) throws Exception {
      UISettingForm settingForm = event.getSource();
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      String emailContent = "";
      WebuiRequestContext context = event.getRequestContext();
      ResourceBundle res = context.getApplicationResourceBundle();
      UIFormInputWithActions formInputWithActions = settingForm.getChildById(settingForm.SET_DEFAULT_EMAIL_TAB);
      UIFormWYSIWYGInput input = null;
      if (id.equals("0")) {
        emailContent = res.getString("SendEmail.AddNewQuestion.Default");
        input = (UIFormWYSIWYGInput) ((UIFormInputWithActions) formInputWithActions.getChildById(settingForm.SET_DEFAULT_ADDNEW_QUESTION_TAB)).getChildById(EMAIL_DEFAULT_ADD_QUESTION);
        input.setValue(emailContent);
      } else if (id.equals("1")) {
        emailContent = res.getString("SendEmail.ResponseQuestion.Default");
        input = (UIFormWYSIWYGInput) ((UIFormInputWithActions) formInputWithActions.getChildById(settingForm.SET_DEFAULT_EDIT_QUESTION_TAB)).getChildById(EMAIL_DEFAULT_EDIT_QUESTION);
        input.setValue(emailContent);
      } else {
        emailContent = res.getString("SendEmail.MoveQuetstion.Default");
        input = (UIFormWYSIWYGInput) ((UIFormInputWithActions) formInputWithActions.getChildById(settingForm.SET_EMAIL_MOVE_QUESTION_TAB)).getChildById(EMAIL_MOVE_QUESTION);
        input.setValue(emailContent);
      }

      settingForm.isResetMail = true;
      settingForm.indexOfTab = Integer.parseInt(id);
      context.addUIComponentToUpdateByAjax(settingForm);
    }
  }

  static public class ChildTabChangeActionListener extends EventListener<UISettingForm> {
    public void execute(Event<UISettingForm> event) throws Exception {
      UISettingForm settingForm = event.getSource();
      String[] tabId = event.getRequestContext().getRequestParameter(OBJECTID).split("/");
      String tab = tabId[0];
      int id = Integer.parseInt(tabId[1]);
      if (tab.equals("parent")) {
        settingForm.isResetMail = false;
        if (id == 0)
          settingForm.tabSelected = settingForm.DISPLAY_TAB;
        else if (id == 2)
          settingForm.tabSelected = DISCUSSION_TAB;
        else if (id == 3)
          settingForm.tabSelected = settingForm.CATEGORY_SCOPING;
        else
          settingForm.tabSelected = settingForm.SET_DEFAULT_EMAIL_TAB;
      } else {
        settingForm.indexOfTab = id;
        settingForm.isResetMail = true;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(settingForm.getParent());
    }
  }

  static public class SelectCategoryForumActionListener extends EventListener<UISettingForm> {
    public void execute(Event<UISettingForm> event) throws Exception {
      UISettingForm settingForm = event.getSource();
      UIAnswersPortlet uiPortlet = settingForm.getAncestorOfType(UIAnswersPortlet.class);
      try {
        UIPopupContainer watchContainer = settingForm.getAncestorOfType(UIPopupContainer.class);
        UISelectCategoryForumForm listCateForm = settingForm.openPopup(watchContainer, UISelectCategoryForumForm.class, 400, 0);
        listCateForm.setListCategory();
      } catch (Exception e) {
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
        UISelectCategoryForumForm listCateForm = popupAction.createUIComponent(UISelectCategoryForumForm.class, null, null);
        listCateForm.setListCategory();
        popupAction.activate(listCateForm, 400, 400);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      }
    }
  }

  static public class CancelActionListener extends EventListener<UISettingForm> {
    public void execute(Event<UISettingForm> event) throws Exception {
      UISettingForm settingForm = event.getSource();
      UIAnswersPortlet uiPortlet = settingForm.getAncestorOfType(UIAnswersPortlet.class);
      uiPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.getChild(UIAnswersContainer.class));
    }
  }
}
