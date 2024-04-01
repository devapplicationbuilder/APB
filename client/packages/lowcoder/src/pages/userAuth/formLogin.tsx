import { FormInput, PasswordInput } from "lowcoder-design";
import {
  AuthBottomView,
  ConfirmButton,
  FormWrapperMobile,
  LoginCardTitle,
  StyledRouteLink,
} from "pages/userAuth/authComponents";
import React, { useContext, useState, useEffect, useMemo } from "react";
import styled from "styled-components";
import UserApi from "api/userApi";
import { useRedirectUrl } from "util/hooks";
import { checkEmailValid, checkPhoneValid } from "util/stringUtils";
import { UserConnectionSource } from "@lowcoder-ee/constants/userConstants";
import { trans } from "i18n";
import { AuthContext, useAuthSubmit } from "pages/userAuth/authUtils";
import { ThirdPartyAuth } from "pages/userAuth/thirdParty/thirdPartyAuth";
import { AUTH_REGISTER_URL, ORG_AUTH_REGISTER_URL } from "constants/routesURL";
import { useLocation, useParams } from "react-router-dom";

const AccountLoginWrapper = styled(FormWrapperMobile)`
  display: flex;
  flex-direction: column;
  margin-bottom: 106px;
`;

type FormLoginProps = {
  organizationId?: string;
}

export default function FormLogin(props: FormLoginProps) {
  const [account, setAccount] = useState("");
  const [password, setPassword] = useState("");
  const redirectUrl = useRedirectUrl();
  const { systemConfig, inviteInfo, fetchUserAfterAuthSuccess } = useContext(AuthContext);
  const invitationId = inviteInfo?.invitationId;
  const authId = systemConfig?.form.id;
  const location = useLocation();
  const orgId = useParams<any>().orgId;
  const queryParams = new URLSearchParams(location.search);
  let loginId = queryParams.get('loginId');
  let register = queryParams.get('register');
  let token = queryParams.get('token');

  const organizationId = useMemo(() => {
      if (inviteInfo?.invitedOrganizationId) {
          return inviteInfo?.invitedOrganizationId;
      }
      return orgId;
  }, [inviteInfo, orgId])

  const { onSubmit, loading } = useAuthSubmit(
    () =>
      UserApi.formLogin({
        register: register === 'true',
        loginId: loginId ?? "null",
        password: password,
        invitationId: invitationId,
        source: UserConnectionSource.email,
        orgId: organizationId,
        authId,
        token: token ?? "gen"
      }),
    false,
    redirectUrl,
    fetchUserAfterAuthSuccess,
  );

  useEffect(() => {
        // Auto-populate the form fields
        setAccount('null');
        setPassword('null');

        // Trigger form submission after setting the values
        onSubmit();
  }, []); // Empty dependency array to run the effect only once

    return null;
}
