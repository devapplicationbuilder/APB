import { ReactNode } from 'react';

declare type EvalMethods = Record<string, Record<string, Function>>;
declare type CodeType = undefined | "JSON" | "Function";

declare type NodeToValue<NodeT> = NodeT extends Node<infer ValueType> ? ValueType : never;
declare type ValueFn<ValueType> = (...paramValues: any[]) => ValueType;
declare type NodeToNodeFn<NodeT> = Node<ValueFn<NodeToValue<NodeT>>>;
declare type FetchInfo = {
    /**
     * whether any of dependencies' node has executing query
     */
    isFetching: boolean;
    /**
     * whether all dependencies' query have be executed once
     */
    ready: boolean;
};
/**
 * keyof without optional key
 */
declare type NonOptionalKeys<T> = {
    [k in keyof T]-?: undefined extends T[k] ? never : k;
}[keyof T];
/**
 * T extends {[key: string]: Node<any> | undefined}
 */
declare type RecordOptionalNodeToValue<T> = {
    [K in NonOptionalKeys<T>]: NodeToValue<T[K]>;
};
/**
 * the base structure for evaluate
 */
interface Node<T> {
    readonly type: string;
    /**
     * generate a new node, the result is a function with "paramName" as the parameter name
     */
    wrapContext(paramName: string): Node<ValueFn<T>>;
    /**
     * calculate evaluate result
     * @param exposingNodes other dependent Nodes
     */
    evaluate(exposingNodes?: Record<string, Node<unknown>>, methods?: EvalMethods): T;
    /**
     * whether the current or its dependencies have cyclic dependencies
     * this function only can be used after evaluate() has been called
     */
    hasCycle(): boolean;
    /**
     * only available after evaluate
     */
    dependNames(): string[];
    dependValues(): Record<string, unknown>;
    /**
     * filter the real dependencies in the exposingNodes, for boosting the evaluation
     * @warn
     * the results include direct dependencies and dependencies of dependencies.
     * since input node's dependencies don't belong to module in the module feature, the node name may duplicate.
     *
     * FIXME: this should be a protected function.
     */
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}
declare abstract class AbstractNode<T> implements Node<T> {
    readonly type: string;
    evalCache: EvalCache<T>;
    constructor();
    abstract wrapContext(paramName: string): AbstractNode<ValueFn<T>>;
    evaluate(exposingNodes?: Record<string, Node<unknown>>, methods?: EvalMethods): T;
    hasCycle(): boolean;
    abstract getChildren(): Node<unknown>[];
    dependNames(): string[];
    abstract dependValues(): Record<string, unknown>;
    isHitEvalCache(exposingNodes?: Record<string, Node<unknown>>): boolean;
    abstract filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    /**
     * evaluate without cache
     */
    abstract justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): T;
    abstract fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}
interface EvalCache<T> {
    dependingNodeMap?: Map<Node<unknown>, string[]>;
    value?: T;
    inEval?: boolean;
    cyclic?: boolean;
    inIsFetching?: boolean;
    inFilterNodes?: boolean;
}
/**
 * check whether 2 dependingNodeMaps are equal
 * - Node use "===" to check
 * - string[] use deep compare to check
 *
 * @param dependingNodeMap1 first dependingNodeMap
 * @param dependingNodeMap2 second dependingNodeMap
 * @returns whether equals
 */
declare function dependingNodeMapEquals(dependingNodeMap1: Map<Node<unknown>, string[]> | undefined, dependingNodeMap2: Map<Node<unknown>, string[]>): boolean;

interface CachedValue<T> {
    value: T;
    isCached: boolean;
}
declare class CachedNode<T> extends AbstractNode<CachedValue<T>> {
    type: string;
    child: AbstractNode<T>;
    constructor(child: AbstractNode<T>);
    wrapContext(paramName: string): AbstractNode<ValueFn<CachedValue<T>>>;
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): CachedValue<T>;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}
/**
 * return a new node with two input nodes.
 * - if mainNode is never evaled, then (new node).evaluate equals to mainNode.evaluate
 * - if mainNode is never evaled, then (new node).evaluate equals to minorNode.evaluate
 *
 * @remarks
 * encapsulation logic: 2 nodes -> CachedNode(mainNode)+minorNode -> RecordNode({main, minor}) -> FunctionNode
 *
 * @warn improper use may cause unexpected behaviour, be careful.
 * @param mainNode mainNode
 * @param minorNode minorNode
 * @returns the new node
 */
declare function evalNodeOrMinor<T>(mainNode: AbstractNode<T>, minorNode: Node<T>): Node<T>;

/**
 * return a new node, evaluating to a function result with the input node value as the function's input
 */
declare class FunctionNode<T, OutputType> extends AbstractNode<OutputType> {
    readonly child: Node<T>;
    readonly func: (params: T) => OutputType;
    readonly type = "function";
    constructor(child: Node<T>, func: (params: T) => OutputType);
    wrapContext(paramName: string): AbstractNode<ValueFn<OutputType>>;
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): OutputType;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}
declare function withFunction<T, OutputType>(child: Node<T>, func: (params: T) => OutputType): FunctionNode<T, OutputType>;

declare type ValueExtra = {
    segments?: {
        value: string;
        success: boolean;
    }[];
};
declare class ValueAndMsg<T> {
    value: T;
    msg?: string;
    extra?: ValueExtra;
    midValue?: any;
    constructor(value: T, msg?: string, extra?: ValueExtra, midValue?: any);
    hasError(): boolean;
    getMsg(displayValueFn?: (value: T) => string): string;
}

interface CodeNodeOptions {
    codeType?: CodeType;
    evalWithMethods?: boolean;
    paramNamesList?: string[][];
}
/**
 * user input node
 *
 * @remarks
 * CodeNode should resolve the cyclic dependency problem
 * we may assume cyclic dependency only imported by CodeNode
 *
 * FIXME(libin): distinguish Json CodeNode，since wrapContext may cause problems.
 */
declare class CodeNode extends AbstractNode<ValueAndMsg<unknown>> {
    readonly unevaledValue: string;
    readonly options?: CodeNodeOptions | undefined;
    readonly type = "input";
    private readonly codeType?;
    private readonly evalWithMethods;
    private directDepends;
    constructor(unevaledValue: string, options?: CodeNodeOptions | undefined);
    wrapContext(paramName: string): AbstractNode<ValueFn<ValueAndMsg<unknown>>>;
    private convertedValue;
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    filterDirectDepends(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): ValueAndMsg<unknown>;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}
/**
 * generate node for unevaledValue
 */
declare function fromUnevaledValue(unevaledValue: string): FunctionNode<ValueAndMsg<unknown>, unknown>;

/**
 * evaluate to get FetchInfo or fetching status
 */
declare class FetchCheckNode extends AbstractNode<FetchInfo> {
    readonly child: Node<unknown>;
    readonly type = "fetchCheck";
    constructor(child: Node<unknown>);
    wrapContext(paramName: string): AbstractNode<ValueFn<FetchInfo>>;
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    justEval(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
}
declare function isFetching(node: Node<unknown>): Node<FetchInfo>;

declare type RecordNodeToValue<T> = {
    [K in keyof T]: NodeToValue<T[K]>;
};
/**
 * the evaluated value is the record constructed by the children nodes
 */
declare class RecordNode<T extends Record<string, Node<unknown>>> extends AbstractNode<RecordNodeToValue<T>> {
    readonly children: T;
    readonly type = "record";
    constructor(children: T);
    wrapContext(paramName: string): AbstractNode<ValueFn<RecordNodeToValue<T>>>;
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods?: EvalMethods): RecordNodeToValue<T>;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): {
        isFetching: boolean;
        ready: boolean;
    };
}
declare function fromRecord<T extends Record<string, Node<unknown>>>(record: T): RecordNode<T>;

/**
 * directly provide data
 */
declare class SimpleNode<T> extends AbstractNode<T> {
    readonly value: T;
    readonly type = "simple";
    constructor(value: T);
    wrapContext(paramName: string): SimpleNode<() => T>;
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    justEval(exposingNodes: Record<string, Node<unknown>>): T;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): {
        isFetching: boolean;
        ready: boolean;
    };
}
/**
 * provide simple value, don't need to eval
 */
declare function fromValue<T>(value: T): SimpleNode<T>;

declare class WrapNode<T> extends AbstractNode<T> {
    readonly delegate: Node<T>;
    readonly moduleExposingNodes: Record<string, Node<unknown>>;
    readonly moduleExposingMethods?: EvalMethods | undefined;
    readonly inputNodes?: Record<string, string | Node<unknown>> | undefined;
    readonly type = "wrap";
    constructor(delegate: Node<T>, moduleExposingNodes: Record<string, Node<unknown>>, moduleExposingMethods?: EvalMethods | undefined, inputNodes?: Record<string, string | Node<unknown>> | undefined);
    wrapContext(paramName: string): AbstractNode<ValueFn<T>>;
    private wrap;
    filterNodes(exposingNodes: Record<string, Node<unknown>>): Map<Node<unknown>, string[]>;
    justEval(exposingNodes: Record<string, Node<unknown>>, methods: EvalMethods): T;
    fetchInfo(exposingNodes: Record<string, Node<unknown>>): FetchInfo;
    getChildren(): Node<unknown>[];
    dependValues(): Record<string, unknown>;
}

declare function transformWrapper<T>(transformFn: (value: unknown) => T, defaultValue?: T): (valueAndMsg: ValueAndMsg<unknown>) => ValueAndMsg<T>;

declare function relaxedJSONToJSON(text: string, compact: boolean): string;

declare function isDynamicSegment(segment: string): boolean;
declare function getDynamicStringSegments(dynamicString: string): string[];

declare function clearMockWindow(): void;
interface SandBoxOption {
    /**
     * disable all limit, like running in host
     */
    disableLimit?: boolean;
    refErrWhenNotExist?: boolean;
}
declare function evalFunc(functionBody: string, context: any, methods?: EvalMethods, options?: SandBoxOption): any;

declare function evalStyle(id: string, css: string[]): void;
declare function clearStyleEval(id?: string): void;

declare function nodeIsRecord(node: Node<unknown>): node is RecordNode<Record<string, Node<unknown>>>;

declare function changeDependName(unevaledValue: string, oldName: string, name: string, isFunction?: boolean): string;

declare type JSONValue = string | number | boolean | JSONObject | JSONArray | null;
interface JSONObject {
    [x: string]: JSONValue | undefined;
}
declare type JSONArray = Array<JSONValue>;

declare enum CompActionTypes {
    CHANGE_VALUE = "CHANGE_VALUE",
    RENAME = "RENAME",
    MULTI_CHANGE = "MULTI_CHANGE",
    ADD_CHILD = "ADD_CHILD",
    DELETE_COMP = "DELETE_COMP",
    ONLY_EVAL = "NEED_EVAL",
    ASYNC = "ASYNC",
    ASYNC_END = "ASYNC_END",
    UPDATE_NODES_V2 = "UPDATE_NODES_V2",
    EXECUTE_QUERY = "EXECUTE_QUERY",
    TRIGGER_MODULE_EVENT = "TRIGGER_MODULE_EVENT",
    /**
     * this action can pass data to the comp by name
     */
    ROUTE_BY_NAME = "ROUTE_BY_NAME",
    /**
     * execute action with context. for example, buttons in table's column should has currentRow as context
     * FIXME: this is a broadcast message, better to be improved by a heritage mechanism.
     */
    UPDATE_ACTION_CONTEXT = "UPDATE_ACTION_CONTEXT",
    /**
     * comp-specific action can be placed not globally.
     * use CUSTOM uniformly.
     */
    CUSTOM = "CUSTOM",
    /**
     * broadcast other actions in comp tree structure.
     * used for encapsulate MultiBaseComp
     */
    BROADCAST = "BROADCAST"
}
declare type ExtraActionType = "layout" | "delete" | "add" | "modify" | "rename" | "recover";
declare type ActionExtraInfo = {
    compInfos: {
        compName: string;
        compType: string;
        type: ExtraActionType;
    }[];
};
declare type ActionPriority = "sync" | "defer";
interface ActionCommon {
    path: Array<string>;
    skipHistory?: boolean;
    extraInfo?: ActionExtraInfo;
    priority?: ActionPriority;
}
interface CustomAction<DataType = JSONValue> extends ActionCommon {
    type: CompActionTypes.CUSTOM;
    value: DataType;
}
interface ChangeValueAction<DataType extends JSONValue = JSONValue> extends ActionCommon {
    type: CompActionTypes.CHANGE_VALUE;
    value: DataType;
}
interface RenameAction extends ActionCommon {
    type: CompActionTypes.RENAME;
    oldName: string;
    name: string;
}
interface BroadcastAction<Action extends ActionCommon = ActionCommon> extends ActionCommon {
    type: CompActionTypes.BROADCAST;
    action: Action;
}
interface MultiChangeAction extends ActionCommon {
    type: CompActionTypes.MULTI_CHANGE;
    changes: Record<string, CompAction>;
}
interface AddChildAction extends ActionCommon {
    type: CompActionTypes.ADD_CHILD;
    key: string;
    value: JSONValue;
}
interface SimpleCompAction extends ActionCommon {
    type: CompActionTypes.DELETE_COMP | CompActionTypes.ONLY_EVAL;
}
interface ExecuteQueryAction extends ActionCommon {
    type: CompActionTypes.EXECUTE_QUERY;
    queryName?: string;
    args?: Record<string, unknown>;
    afterExecFunc?: () => void;
}
interface TriggerModuleEventAction extends ActionCommon {
    type: CompActionTypes.TRIGGER_MODULE_EVENT;
    name: string;
}
interface RouteByNameAction extends ActionCommon {
    type: CompActionTypes.ROUTE_BY_NAME;
    name: string;
    action: CompAction<any>;
}
interface UpdateNodesV2Action extends ActionCommon {
    type: CompActionTypes.UPDATE_NODES_V2;
    value: any;
}
declare type ActionContextType = Record<string, unknown>;
interface UpdateActionContextAction extends ActionCommon {
    type: CompActionTypes.UPDATE_ACTION_CONTEXT;
    context: ActionContextType;
}
declare type CompAction<DataType extends JSONValue = JSONValue> = CustomAction<unknown> | ChangeValueAction<DataType> | BroadcastAction | RenameAction | AddChildAction | MultiChangeAction | SimpleCompAction | ExecuteQueryAction | UpdateActionContextAction | RouteByNameAction | TriggerModuleEventAction | UpdateNodesV2Action;

declare function customAction<DataType>(value: DataType): CustomAction<DataType>;
declare function updateActionContextAction(context: ActionContextType): BroadcastAction<UpdateActionContextAction>;
/**
 * check if it's current custom action.
 * keep type safe via generics, users should keep type the same as T, otherwise may cause bug.
 */
declare function isMyCustomAction<T>(action: CompAction, type: string): action is CustomAction<T>;
declare function isCustomAction<T>(action: CompAction, type: string): action is CustomAction<T>;
/**
 * The action of execute query.
 * path route to the query exactly.
 * RootComp will change the path correctly when queryName is passed.
 */
declare function executeQueryAction(props: {
    args?: Record<string, unknown>;
    afterExecFunc?: () => void;
}): ExecuteQueryAction;
declare function triggerModuleEventAction(name: string): TriggerModuleEventAction;
/**
 * better to use comp.dispatchChangeValueAction to keep type safe
 */
declare function changeValueAction(value: JSONValue): ChangeValueAction;
declare function isBroadcastAction<T extends CompAction>(action: CompAction, type: T["type"]): action is BroadcastAction<T>;
declare function renameAction(oldName: string, name: string): BroadcastAction<RenameAction>;
declare function routeByNameAction(name: string, action: CompAction<any>): RouteByNameAction;
declare function addChildAction(key: string, value: JSONValue): AddChildAction;
declare function multiChangeAction(changes: Record<string, CompAction>): MultiChangeAction;
declare function deleteCompAction(): SimpleCompAction;
declare function onlyEvalAction(): SimpleCompAction;
declare function wrapChildAction(childName: string, action: CompAction): CompAction;
declare function isChildAction(action: CompAction): boolean;
declare function unwrapChildAction(action: CompAction): [string, CompAction];
declare function changeChildAction(childName: string, value: JSONValue): CompAction;
declare function updateNodesV2Action(value: any): UpdateNodesV2Action;
declare function wrapActionExtraInfo<T extends CompAction>(action: T, extraCompInfos: ActionExtraInfo["compInfos"]): T;
declare function deferAction<T extends CompAction>(action: T): T;

declare type OptionalNodeType = Node<unknown> | undefined;
declare type DispatchType = (action: CompAction) => void;
/**
 * Comp architecture: https://codemore.feishu.cn/wiki/wikcngXolZsjqCjIpRA5M6sw65f
 * FIXME: replace as a github page
 */
interface Comp<ViewReturn = any, DataType extends JSONValue = JSONValue, NodeType extends OptionalNodeType = OptionalNodeType> {
    dispatch: DispatchType;
    getView(): ViewReturn;
    getPropertyView(): ReactNode;
    reduce(action: CompAction): this;
    node(): NodeType;
    toJsonValue(): DataType;
    /**
     * change current comp's dispatch function.
     * used when the comp is moved across the tree structure.
     */
    changeDispatch(dispatch: DispatchType): this;
}
declare abstract class AbstractComp<ViewReturn = any, DataType extends JSONValue = JSONValue, NodeType extends OptionalNodeType = OptionalNodeType> implements Comp<ViewReturn, DataType, NodeType> {
    dispatch: DispatchType;
    constructor(params: CompParams);
    abstract getView(): ViewReturn;
    abstract getPropertyView(): ReactNode;
    abstract toJsonValue(): DataType;
    abstract reduce(_action: CompAction): this;
    abstract nodeWithoutCache(): NodeType;
    changeDispatch(dispatch: DispatchType): this;
    /**
     * trigger changeValueAction, type safe
     */
    dispatchChangeValueAction(value: DataType): void;
    /**
     * don't override the function, override nodeWithout function instead
     * FIXME: node reference mustn't be changed if this object is changed
     */
    node(): NodeType;
}
declare type OptionalComp<T = any> = Comp<T> | undefined;
declare type CompConstructor<ViewReturn = any, DataType extends JSONValue = any, NodeType extends OptionalNodeType = OptionalNodeType> = new (params: CompParams<DataType>) => Comp<ViewReturn, DataType, NodeType>;
/**
 * extract constructor's generic type
 */
declare type ConstructorToView<T> = T extends CompConstructor<infer ViewReturn> ? ViewReturn : never;
declare type ConstructorToComp<T> = T extends new (params: CompParams<any>) => infer X ? X : never;
declare type ConstructorToDataType<T> = T extends new (params: CompParams<infer DataType>) => any ? DataType : never;
declare type ConstructorToNodeType<T> = ConstructorToComp<T> extends Comp<any, any, infer NodeType> ? NodeType : never;
declare type RecordConstructorToComp<T> = {
    [K in keyof T]: ConstructorToComp<T[K]>;
};
declare type RecordConstructorToView<T> = {
    [K in keyof T]: ConstructorToView<T[K]>;
};
interface CompParams<DataType extends JSONValue = JSONValue> {
    dispatch?: (action: CompAction) => void;
    value?: DataType;
}

/**
 * MultiBaseCompConstructor with abstract function implemented
 */
declare type MultiCompConstructor = new (params: CompParams<any>) => MultiBaseComp<any, any, any> & Comp<any, any, any>;
/**
 * wrap a dispatch as a child dispatch
 *
 * @param dispatch input dispatch
 * @param childName the key of the child dispatch
 * @returns a wrapped dispatch with the child dispatch
 */
declare function wrapDispatch(dispatch: DispatchType | undefined, childName: string): DispatchType;
declare type ExtraNodeType = {
    node: Record<string, Node<any>>;
    updateNodeFields: (value: any) => Record<string, any>;
};
/**
 * the core class of multi function
 * build the tree structure of comps
 * @remarks
 * functions can be cached if needed.
 **/
declare abstract class MultiBaseComp<ChildrenType extends Record<string, Comp<unknown>> = Record<string, Comp<unknown>>, DataType extends JSONValue = JSONValue, NodeType extends OptionalNodeType = OptionalNodeType> extends AbstractComp<any, DataType, NodeType> {
    readonly children: ChildrenType;
    constructor(params: CompParams<DataType>);
    abstract parseChildrenFromValue(params: CompParams<DataType>): ChildrenType;
    reduce(action: CompAction): this;
    protected reduceOrUndefined(action: CompAction): this | undefined;
    setChild(childName: keyof ChildrenType, newChild: Comp): this;
    protected setChildren(children: Record<string, Comp>): this;
    /**
     * extended interface.
     *
     * @return node for additional node, updateNodeFields for handling UPDATE_NODE event
     * FIXME: make type safe
     */
    protected extraNode(): ExtraNodeType | undefined;
    protected childrenNode(): {
        [key: string]: Node<unknown>;
    };
    nodeWithoutCache(): NodeType;
    changeDispatch(dispatch: DispatchType): this;
    toJsonValue(): DataType;
    autoHeight(): boolean;
}
declare function mergeExtra(e1: ExtraNodeType | undefined, e2: ExtraNodeType): ExtraNodeType;

/**
 * maintainer a JSONValue, nothing else
 */
declare abstract class SimpleAbstractComp<ViewReturn extends JSONValue> extends AbstractComp<any, ViewReturn, Node<ViewReturn>> {
    value: ViewReturn;
    constructor(params: CompParams<ViewReturn>);
    protected abstract getDefaultValue(): ViewReturn;
    /**
     * may override this to implement compatibility
     */
    protected oldValueToNew(value?: ViewReturn): ViewReturn | undefined;
    reduce(action: CompAction): this;
    nodeWithoutCache(): SimpleNode<ViewReturn>;
    exposingNode(): Node<ViewReturn>;
    toJsonValue(): ViewReturn;
}
declare abstract class SimpleComp<ViewReturn extends JSONValue> extends SimpleAbstractComp<ViewReturn> {
    getView(): ViewReturn;
}

interface LocaleInfo {
    locale: string;
    language: string;
    region?: string;
}
declare const i18n: {
    locale: string;
    language: string;
    region?: string | undefined;
    locales: string[];
};
declare function getValueByLocale<T>(defaultValue: T, func: (info: LocaleInfo) => T | undefined): T;
declare type AddDot<T extends string> = T extends "" ? "" : `.${T}`;
declare type ValidKey<T> = Exclude<keyof T, symbol>;
declare type NestedKey<T> = (T extends object ? {
    [K in ValidKey<T>]: `${K}${AddDot<NestedKey<T[K]>>}`;
}[ValidKey<T>] : "") extends infer D ? Extract<D, string> : never;
declare type AddPrefix<T, P extends string> = {
    [K in keyof T as K extends string ? `${P}${K}` : never]: T[K];
};
declare const globalMessages: AddPrefix<{}, "@">;
declare type GlobalMessageKey = NestedKey<typeof globalMessages>;
declare type VariableValue = string | number | boolean | Date;
declare class Translator<Messages extends object> {
    private readonly messages;
    readonly language: string;
    constructor(fileData: object, filterLocales?: string);
    trans(key: NestedKey<Messages> | GlobalMessageKey, variables?: Record<string, VariableValue>): string;
    private getMessage;
}
declare function getI18nObjects<I18nObjects>(fileData: object, filterLocales?: string): I18nObjects;

export { AbstractComp, AbstractNode, ActionContextType, ActionExtraInfo, ActionPriority, AddChildAction, BroadcastAction, CachedNode, ChangeValueAction, CodeNode, CodeNodeOptions, CodeType, Comp, CompAction, CompActionTypes, CompConstructor, CompParams, ConstructorToComp, ConstructorToDataType, ConstructorToNodeType, ConstructorToView, CustomAction, DispatchType, EvalMethods, ExecuteQueryAction, ExtraActionType, FetchCheckNode, FetchInfo, FunctionNode, MultiBaseComp, MultiChangeAction, MultiCompConstructor, Node, NodeToNodeFn, NodeToValue, OptionalComp, OptionalNodeType, RecordConstructorToComp, RecordConstructorToView, RecordNode, RecordNodeToValue, RecordOptionalNodeToValue, RenameAction, RouteByNameAction, SimpleAbstractComp, SimpleComp, SimpleCompAction, SimpleNode, Translator, TriggerModuleEventAction, UpdateActionContextAction, UpdateNodesV2Action, ValueAndMsg, ValueFn, WrapNode, addChildAction, changeChildAction, changeDependName, changeValueAction, clearMockWindow, clearStyleEval, customAction, deferAction, deleteCompAction, dependingNodeMapEquals, evalFunc, evalNodeOrMinor, evalStyle, executeQueryAction, fromRecord, fromUnevaledValue, fromValue, getDynamicStringSegments, getI18nObjects, getValueByLocale, i18n, isBroadcastAction, isChildAction, isCustomAction, isDynamicSegment, isFetching, isMyCustomAction, mergeExtra, multiChangeAction, nodeIsRecord, onlyEvalAction, relaxedJSONToJSON, renameAction, routeByNameAction, transformWrapper, triggerModuleEventAction, unwrapChildAction, updateActionContextAction, updateNodesV2Action, withFunction, wrapActionExtraInfo, wrapChildAction, wrapDispatch };