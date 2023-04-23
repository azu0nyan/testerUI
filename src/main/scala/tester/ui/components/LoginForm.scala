package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import slinky.core.annotations.react
import org.scalajs.dom._
import slinky.core.facade.Hooks.useState
import slinky.core.facade.ReactElement
import typings.antDesignIcons.components.AntdIcon
import typings.antDesignIconsSvg._
import typings.antd.antdStrings
import typings.antd.components.{List => AntList, _}
import typings.antd.components.Form.{Form => FormItem}
import typings.antd.libFormFormMod.{FormLayout, useForm}
import typings.antd.libFormHooksUseFormMod.FormInstance
import typings.antd.libGridColMod.ColProps
import typings.rcFieldForm.esInterfaceMod
import typings.rcFieldForm.esInterfaceMod.{AggregationRule, BaseRule, Rule, RuleObject}
import typings.rcFieldForm.esUtilsMessagesMod.defaultValidateMessages
import typings.react.mod.CSSProperties


@react object LoginForm {
  type Props = Unit

  class Values(val login: String, val password: String) extends js.Object


  val component = FunctionalComponent[Props] { _ =>
    val form: FormInstance[Values] = useForm[Values]().head

    Form[Values]()
      .form(form)
      .name("loginForm")
      .labelCol(ColProps().setSpan(5))
      .wrapperCol(ColProps().setSpan(12))
      .style(CSSProperties().setMaxWidth("600"))
      //      .layout(FormLayout.horizontal)
      .onFinish(store => console.log(s"sadsad ${store.password} ${store.login}"))(
        FormItem()
          .label("Логин")
          .name("login")
          .rules(
            js.Array[Rule](
              AggregationRule().setRequired(true).setMessage("Логин не может быть пустым"),
            )
          )(
            Input()
          ),
        FormItem()
          .label("Пароль")
          .name("password")
          .rules(
            js.Array[Rule](
              AggregationRule().setRequired(true).setMessage("Пароль не может быть пустым"),
            )
          )(
            Input.Password()
          ),
        FormItem()
          .wrapperCol(ColProps().setOffset(5).setSpan(12))
          .name("Submit")(Button().htmlType(antdStrings.submit).`type`(antdStrings.primary)("Log in"))
      )


  }

}
