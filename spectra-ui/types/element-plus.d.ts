export {};

declare global {
    interface TreeSelectNode {
        pathLabels?: string[];
    }

    type NodeParam = TreeSelectNode | TreeSelectNode[] | undefined;

    type DataParam = AreaNode | AreaNode[] | undefined;
}
