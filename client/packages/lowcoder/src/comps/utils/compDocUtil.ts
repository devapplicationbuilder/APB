import { UICompType } from "comps/uiCompRegistry";
import { trans } from "i18n";

export function getComponentDocUrl(compType: UICompType) {
  const { protocol, hostname, port } = window.location;
  var newUrl = '';

  if (port) {
      newUrl = `${protocol}//${hostname}:${port}/components/${compType}`;
  } else {
    newUrl = `${protocol}//${hostname}/components/${compType}`;
  }

  if (!compType) {
    return "";
  }
  switch (compType) {
    case "module":
      return trans("docUrls.module");
    default:
      return newUrl;
  }
}
export function getComponentPlaygroundUrl(compType: UICompType) {
  const { protocol, hostname, port } = window.location;
  var newUrl = '';

  if (port) {
      newUrl = `${protocol}//${hostname}:${port}/playground/${compType}/1`;
  } else {
      newUrl = `${protocol}//${hostname}_AUTH/playground/${compType}/1`;
  }

  if (!compType) {
    return "";
  }
  switch (compType) {
    case "module":
      return trans("docUrls.module");
    default:
      return newUrl;
  }
}
