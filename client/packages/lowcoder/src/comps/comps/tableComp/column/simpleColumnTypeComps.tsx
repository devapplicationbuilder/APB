import { ColumnTypeCompBuilder } from "comps/comps/tableComp/column/columnTypeCompBuilder";
import { ActionSelectorControlInContext } from "comps/controls/actionSelector/actionSelectorControl";
import { BoolCodeControl, StringControl } from "comps/controls/codeControl";
import { dropdownControl } from "comps/controls/dropdownControl";
import { disabledPropertyView, loadingPropertyView } from "comps/utils/propertyUtils";
import { trans } from "i18n";
import { useStyle } from "comps/controls/styleControl";
import { ButtonStyle } from "comps/controls/styleControlConstants";
import { Button100 } from "comps/comps/buttonComp/buttonCompConstants";
import { IconControl } from "comps/controls/iconControl";
import styled from "styled-components";

export const ColumnValueTooltip = trans("table.columnValueTooltip");

const ButtonTypeOptions = [
  {
    label: trans("table.primaryButton"),
    value: "primary",
  },
  {
    label: trans("table.defaultButton"),
    value: "default",
  },
] as const;

const IconWrapper = styled.div`
  display: flex;
`;

export const ButtonComp = (function () {
    const childrenMap = {
        text: StringControl,
        buttonType: dropdownControl(ButtonTypeOptions, "primary"),
        onClick: ActionSelectorControlInContext,
        loading: BoolCodeControl,
        disabled: BoolCodeControl,
        prefixIcon: IconControl,
        centerIcon: IconControl,
        suffixIcon: IconControl,
        tooltip: StringControl,
    };
    return new ColumnTypeCompBuilder(
        childrenMap,
        (props) => {
            const ButtonStyled = () => {
                const style = useStyle(ButtonStyle);

                return (
                    <Button100
                        type={props.buttonType}
                        onClick={props.onClick}
                        loading={props.loading}
                        disabled={props.disabled}
                        $buttonStyle={props.buttonType !== "primary" || props.centerIcon ? undefined : style}
                        title={props.tooltip}
                    >
                        {props.prefixIcon && <IconWrapper>{props.prefixIcon}</IconWrapper>}
                        {props.centerIcon && <IconWrapper>{props.centerIcon}</IconWrapper>}
                        {props.text || (props.prefixIcon || props.suffixIcon || props.centerIcon ? undefined : " ")}
                        {props.suffixIcon && <IconWrapper>{props.suffixIcon}</IconWrapper>}
                    </Button100>
                );
            };

            return <ButtonStyled />;
        },
        (nodeValue) => nodeValue.text.value
    )
        .setPropertyViewFn((children) => (
            <>
                {children.text.propertyView({
                    label: trans("table.columnValue"),
                    tooltip: ColumnValueTooltip,
                })}
                {children.buttonType.propertyView({
                    label: trans("table.type"),
                    radioButton: true,
                })}
                {children.prefixIcon.propertyView({
                    label: trans("button.prefixIcon"),
                })}
                {children.centerIcon.propertyView({
                    label: trans("button.centerIcon"),
                })}
                {children.suffixIcon.propertyView({
                    label: trans("button.suffixIcon"),
                })}
                {children.tooltip.propertyView({
                    label: trans("labelProp.tooltip"),
                })}
                {loadingPropertyView(children)}
                {disabledPropertyView(children)}
                {children.onClick.propertyView({
                    label: trans("table.action"),
                    placement: "table",
                })}
            </>
        ))
        .build();
})();
