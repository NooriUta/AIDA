import type { NodeTypes } from '@xyflow/react';

import { SchemaNode }       from './nodes/SchemaNode';
import { SchemaGroupNode }  from './nodes/SchemaGroupNode';
import { PackageNode }      from './nodes/PackageNode';
import { TableNode }        from './nodes/TableNode';
import { RoutineNode }      from './nodes/RoutineNode';
import { RecordNode }       from './nodes/RecordNode';
import { ColumnNode }       from './nodes/ColumnNode';
import { StatementNode }    from './nodes/StatementNode';
import { RoutineGroupNode }  from './nodes/RoutineGroupNode';
import { PackageGroupNode }  from './nodes/PackageGroupNode';
import { ApplicationNode }  from './nodes/ApplicationNode';
import { DatabaseNode }     from './nodes/DatabaseNode';
import { L1SchemaNode }     from './nodes/L1SchemaNode';

export const NODE_TYPES: NodeTypes = {
  schemaNode:       SchemaNode       as NodeTypes[string],
  schemaGroupNode:  SchemaGroupNode  as NodeTypes[string],
  packageNode:      PackageNode      as NodeTypes[string],
  tableNode:        TableNode        as NodeTypes[string],
  routineNode:      RoutineNode      as NodeTypes[string],
  recordNode:       RecordNode       as NodeTypes[string],
  routineGroupNode: RoutineGroupNode as NodeTypes[string],
  packageGroupNode: PackageGroupNode as NodeTypes[string],
  statementNode:    StatementNode    as NodeTypes[string],
  columnNode:       ColumnNode       as NodeTypes[string],
  atomNode:         ColumnNode       as NodeTypes[string],
  applicationNode:  ApplicationNode  as NodeTypes[string],
  databaseNode:     DatabaseNode     as NodeTypes[string],
  l1SchemaNode:     L1SchemaNode     as NodeTypes[string],
};
